package com.liuguanyi.liumusic.core.remoteconfig

import android.content.Context
import com.liuguanyi.liumusic.core.music.model.MusicSource

object LiuMusicRemoteConfigPolicy {
    fun activeConfig(context: Context): LiuMusicRemoteConfig = effectiveConfig(
        consentEnabled = LiuMusicRemoteConfigConsent.enabled(context),
        status = LiuMusicRemoteConfigRuntime.status.value,
    )

    fun capabilityEnabled(context: Context, capability: String): Boolean =
        capability !in activeConfig(context).disabledCapabilities

    fun providerPlaybackEnabled(context: Context, source: MusicSource): Boolean = providerPlaybackEnabled(
        config = activeConfig(context),
        source = source,
    )

    internal fun effectiveConfig(
        consentEnabled: Boolean,
        status: LiuMusicRemoteConfigStatus,
    ): LiuMusicRemoteConfig = if (consentEnabled && status.source == LiuMusicRemoteConfigSource.VerifiedRemote) {
        status.config
    } else {
        LiuMusicRemoteConfigDefaults.Config
    }

    internal fun providerPlaybackEnabled(config: LiuMusicRemoteConfig, source: MusicSource): Boolean = when (source) {
        MusicSource.QQMusic -> "qq_playback" !in config.disabledCapabilities
        MusicSource.Kugou -> "kugou_playback" !in config.disabledCapabilities
        MusicSource.Kuwo -> "kuwo_playback" !in config.disabledCapabilities
        MusicSource.Bilibili -> "bilibili_playback" !in config.disabledCapabilities
        else -> true
    }
}
