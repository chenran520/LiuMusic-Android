package com.liuguanyi.liumusic.core.remoteconfig

import android.content.Context
import com.liuguanyi.liumusic.LiuMusicAppVisibility
import com.liuguanyi.liumusic.core.network.LiuMusicGitHubRouting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object LiuMusicRemoteConfigRuntime {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshMutex = Mutex()
    private val mutableStatus = MutableStateFlow(LiuMusicRemoteConfigStatus())
    val status: StateFlow<LiuMusicRemoteConfigStatus> = mutableStatus.asStateFlow()

    @Volatile
    private var client: LiuMusicRemoteConfigClient? = null

    @Volatile
    private var versionCode: Int = 0

    @Volatile
    private var periodicRefreshJob: Job? = null

    @Volatile
    private var overlayJob: Job? = null

    fun initializeAndRefresh(context: Context, versionCode: Int, force: Boolean = false) {
        val appContext = context.applicationContext
        synchronized(this) {
            this.versionCode = versionCode
            if (client == null) {
                client = LiuMusicRemoteConfigClient(
                    store = LiuMusicRemoteConfigStore(appContext),
                    verifier = LiuMusicRemoteConfigVerifier(),
                    routing = LiuMusicGitHubRouting(appContext),
                )
            }
            periodicRefreshJob?.cancel()
            periodicRefreshJob = scope.launch {
                while (isActive && LiuMusicRemoteConfigConsent.enabled(appContext)) {
                    delay(LiuMusicRemoteConfigRefreshIntervalMs)
                    if (LiuMusicRemoteConfigConsent.enabled(appContext) && LiuMusicAppVisibility.isForeground) {
                        refresh(force = true)
                    }
                }
            }
            // Republish UI text overrides whenever the effective config changes,
            // so revoking consent or a rolled-back config drops them immediately.
            if (overlayJob == null) {
                overlayJob = scope.launch {
                    mutableStatus.collect { status ->
                        val effective = LiuMusicRemoteConfigPolicy.effectiveConfig(
                            consentEnabled = LiuMusicRemoteConfigConsent.enabled(appContext),
                            status = status,
                        )
                        LiuMusicHotStrings.update(effective.textOverrides)
                        LiuMusicHotScripts.update(effective.scripts)
                        LiuMusicHotStyles.update(effective.colorOverrides)
                        LiuMusicHotRules.update(effective.rules)
                        LiuMusicHotStrategies.update(effective.strategies)
                    }
                }
            }
        }
        scope.launch {
            mutableStatus.value = client?.load(versionCode) ?: return@launch
            refresh(force = force)
            if (mutableStatus.value.error != null) {
                delay(1_500L)
                refresh(force = true)
            }
        }
    }

    suspend fun refresh(force: Boolean = true) = refreshMutex.withLock {
        val active = client ?: return@withLock
        mutableStatus.value = mutableStatus.value.copy(refreshing = true, error = null)
        mutableStatus.value = active.refresh(versionCode, force).copy(refreshing = false)
    }

    suspend fun clearCache(context: Context? = null) = refreshMutex.withLock {
        if (context != null && !LiuMusicRemoteConfigConsent.enabled(context)) {
            periodicRefreshJob?.cancel()
            periodicRefreshJob = null
        }
        val active = client
        mutableStatus.value = when {
            active != null -> active.clear(versionCode)
            context != null -> {
                LiuMusicRemoteConfigStore(context.applicationContext).clearEnvelope()
                LiuMusicRemoteConfigStatus()
            }
            else -> LiuMusicRemoteConfigStatus()
        }
    }
}

internal const val LiuMusicRemoteConfigRefreshIntervalMs = 2L * 60L * 60L * 1_000L
