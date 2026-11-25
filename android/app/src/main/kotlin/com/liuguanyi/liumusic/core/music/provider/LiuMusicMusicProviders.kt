package com.liuguanyi.liumusic.core.music.provider

import android.content.Context
import com.liuguanyi.liumusic.core.account.NeteaseSessionStore
import com.liuguanyi.liumusic.core.provider.applemusic.AppleMusicApiClient
import com.liuguanyi.liumusic.core.provider.applemusic.AppleMusicSessionStore
import com.liuguanyi.liumusic.core.provider.kugou.KugouProvider
import com.liuguanyi.liumusic.core.provider.kugou.KugouSessionStore
import com.liuguanyi.liumusic.core.provider.kuwo.KuwoProvider
import com.liuguanyi.liumusic.core.provider.kuwo.KuwoSessionStore
import com.liuguanyi.liumusic.core.provider.netease.NeteaseProvider
import com.liuguanyi.liumusic.core.provider.aurora.JooxProvider
import com.liuguanyi.liumusic.core.provider.aurora.HigequProvider
import com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicProvider
import com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicSessionStore
import com.liuguanyi.liumusic.core.provider.bilibili.BilibiliProvider
import com.liuguanyi.liumusic.core.provider.bilibili.BilibiliSessionStore
import com.liuguanyi.liumusic.core.provider.bilibili.BilibiliPlaybackAssociationStore
import com.liuguanyi.liumusic.core.provider.bilibili.BilibiliApiCache
import com.liuguanyi.liumusic.core.provider.spotify.SpotifyClientConfig
import com.liuguanyi.liumusic.core.provider.spotify.SpotifyProvider
import com.liuguanyi.liumusic.core.provider.youtubemusic.YouTubeMusicProvider
import com.liuguanyi.liumusic.core.provider.jellyfin.JellyfinProvider
import com.liuguanyi.liumusic.core.provider.jellyfin.JellyfinSessionStore
import com.liuguanyi.liumusic.core.provider.local.LocalProvider
import com.liuguanyi.liumusic.core.network.LiuMusicHttpClient
import okhttp3.OkHttpClient

/** Creates provider instances that all read their authentication state locally. */
object LiuMusicMusicProviders {
    @Volatile
    private var sharedRegistry: MusicProviderRegistry? = null

    fun create(
        context: Context,
        httpClient: OkHttpClient = LiuMusicHttpClient.shared,
    ): MusicProviderRegistry {
        val appContext = context.applicationContext
        if (httpClient !== LiuMusicHttpClient.shared) return buildRegistry(appContext, httpClient)
        return sharedRegistry ?: synchronized(this) {
            sharedRegistry ?: buildRegistry(appContext, httpClient).also { sharedRegistry = it }
        }
    }

    /** Registry reserved for URL resolution and quality probing. */
    fun createPlayback(
        context: Context,
        httpClient: OkHttpClient = LiuMusicHttpClient.shared,
    ): MusicProviderRegistry {
        val appContext = context.applicationContext
        val nativeProviders = listOf<MusicProvider>(
                NeteaseProvider({ PlaybackAccountStore.neteaseCookie(appContext) }, httpClient),
                JooxProvider(com.liuguanyi.liumusic.core.provider.aurora.AuroraSourceClient(httpClient)),
                HigequProvider(com.liuguanyi.liumusic.core.provider.aurora.AuroraSourceClient(httpClient)),
                LocalProvider(appContext),
        )
        return MusicProviderRegistry(nativeProviders)
    }

    private fun buildRegistry(context: Context, httpClient: OkHttpClient): MusicProviderRegistry {
        val nativeProviders = listOf<MusicProvider>(
                NeteaseProvider(
                    cookieProvider = { NeteaseSessionStore.readCookie(context) },
                    httpClient = httpClient,
                ),
                JooxProvider(com.liuguanyi.liumusic.core.provider.aurora.AuroraSourceClient(httpClient)),
                HigequProvider(com.liuguanyi.liumusic.core.provider.aurora.AuroraSourceClient(httpClient)),
                LocalProvider(context),
        )
        return MusicProviderRegistry(nativeProviders)
    }
}
