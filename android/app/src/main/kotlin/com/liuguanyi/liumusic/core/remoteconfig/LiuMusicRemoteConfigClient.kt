package com.liuguanyi.liumusic.core.remoteconfig

import com.liuguanyi.liumusic.core.network.LiuMusicGitHubRouting
import com.liuguanyi.liumusic.core.network.LiuMusicGitHubSource
import android.util.Log
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

internal class LiuMusicRemoteConfigClient(
    private val store: LiuMusicRemoteConfigStore,
    private val verifier: LiuMusicRemoteConfigVerifier,
    private val routing: LiuMusicGitHubRouting,
    private val now: () -> Long = System::currentTimeMillis,
) {
    fun load(versionCode: Int): LiuMusicRemoteConfigStatus = statusFrom(store.read(), versionCode)

    suspend fun refresh(versionCode: Int, force: Boolean): LiuMusicRemoteConfigStatus =
        withContext(Dispatchers.IO) { refreshOnIo(versionCode, force) }

    private suspend fun refreshOnIo(versionCode: Int, force: Boolean): LiuMusicRemoteConfigStatus {
        val current = store.read()
        val timestamp = now()
        if (!force && timestamp - current.lastCheckedAtEpochMs in 0 until LiuMusicRemoteConfigRefreshIntervalMs) {
            return statusFrom(current, versionCode)
        }
        val errors = mutableListOf<String>()
        for (source in routing.candidates()) {
            val result = runCatching { fetch(source, current, timestamp, versionCode) }
            result.getOrNull()?.let { return it }
            result.exceptionOrNull()?.let { error ->
                val description = error.message?.takeIf(String::isNotBlank) ?: error.javaClass.simpleName
                errors += "${source.label}: $description"
                Log.w("LiuMusicRemoteConfig", "Remote config failed through ${source.label}", error)
            }
        }
        val failed = current.copy(lastCheckedAtEpochMs = timestamp)
        store.write(failed)
        return statusFrom(failed, versionCode).copy(
            error = errors.takeIf(List<String>::isNotEmpty)?.joinToString("；") ?: "所有配置源均不可用",
        )
    }

    fun clear(versionCode: Int): LiuMusicRemoteConfigStatus {
        store.clearEnvelope()
        return load(versionCode)
    }

    private fun fetch(
        source: LiuMusicGitHubSource,
        current: LiuMusicStoredRemoteConfig,
        timestamp: Long,
        versionCode: Int,
    ): LiuMusicRemoteConfigStatus {
        val request = Request.Builder().url(routing.routedUrl(source, LiuMusicGitHubRouting.RemoteConfigUrl)).apply {
            current.etag?.let { header("If-None-Match", it) }
        }.build()
        routing.client(source).newBuilder()
            .cache(null)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
            .newCall(request).execute().use { response ->
            if (response.code == 304 && current.envelope != null) {
                val updated = current.copy(lastCheckedAtEpochMs = timestamp)
                store.write(updated)
                return statusFrom(updated, versionCode)
            }
            if (!response.isSuccessful) throw IOException("${source.label} HTTP ${response.code}")
            val body = response.body
            if (body.contentLength() > LiuMusicRemoteConfigVerifier.MaxEnvelopeBytes) {
                throw IOException("Remote config response is too large")
            }
            val envelope = body.string()
            val verified = verifier.verify(envelope)
            require(verified.config.configVersion >= current.highestConfigVersion) {
                "Remote config rollback rejected"
            }
            val stored = LiuMusicStoredRemoteConfig(
                envelope = envelope,
                etag = response.header("ETag"),
                lastCheckedAtEpochMs = timestamp,
                lastUpdatedAtEpochMs = timestamp,
                highestConfigVersion = maxOf(current.highestConfigVersion, verified.config.configVersion),
            )
            store.write(stored)
            return statusFrom(stored, versionCode)
        }
    }

    private fun statusFrom(stored: LiuMusicStoredRemoteConfig, versionCode: Int): LiuMusicRemoteConfigStatus {
        val verified = stored.envelope?.let { runCatching { verifier.verify(it) }.getOrNull() }
        val config = verified?.config ?: LiuMusicRemoteConfigDefaults.Config
        val source = when {
            verified == null -> LiuMusicRemoteConfigSource.BuiltIn
            config.appliesTo(versionCode) -> LiuMusicRemoteConfigSource.VerifiedRemote
            else -> LiuMusicRemoteConfigSource.VersionInapplicable
        }
        return LiuMusicRemoteConfigStatus(
            source = source,
            config = if (source == LiuMusicRemoteConfigSource.VerifiedRemote) config else LiuMusicRemoteConfigDefaults.Config,
            keyId = verified?.keyId,
            lastCheckedAtEpochMs = stored.lastCheckedAtEpochMs,
            lastUpdatedAtEpochMs = stored.lastUpdatedAtEpochMs,
        )
    }
}
