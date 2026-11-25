package com.liuguanyi.liumusic.playback

import android.content.Context
import com.liuguanyi.liumusic.core.music.model.AudioQualityTier
import com.liuguanyi.liumusic.core.music.model.MusicTrack
import com.liuguanyi.liumusic.core.provider.lxuser.ChkszApiClient
import com.liuguanyi.liumusic.core.provider.lxuser.ChkszApiKeyStore

internal data class ChkszPlaybackResult(val url: String)

class ChkszPlaybackResolver(context: Context) {
    private val appContext = context.applicationContext
    private val client = ChkszApiClient({ ChkszApiKeyStore.read(appContext) })

    fun cacheIdentity(): String = ChkszApiKeyStore.read(appContext).let { key ->
        if (key.isBlank()) "disabled" else "configured:${key.hashCode()}"
    }

    internal fun resolve(songId: Long, quality: AudioQualityTier): ChkszPlaybackResult? =
        client.resolveNetease(songId, quality)?.let(::ChkszPlaybackResult)

    internal fun resolve(track: MusicTrack, quality: AudioQualityTier): ChkszPlaybackResult? =
        client.resolveTrack(track, quality)?.let(::ChkszPlaybackResult)
}
