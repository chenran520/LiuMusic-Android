package com.liuguanyi.liumusic.playback

import android.content.Context
import com.liuguanyi.liumusic.core.account.NeteaseSessionStore
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.core.music.provider.LiuMusicMusicProviders
import com.liuguanyi.liumusic.core.music.provider.PlaybackAccountStore
import com.liuguanyi.liumusic.core.music.provider.MusicProviderRegistry
import com.liuguanyi.liumusic.core.provider.applemusic.AppleMusicSessionStore
import com.liuguanyi.liumusic.core.provider.kugou.KugouSessionStore
import com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicSessionStore
import com.liuguanyi.liumusic.core.provider.bilibili.BilibiliSessionStore
import com.liuguanyi.liumusic.core.provider.bilibili.BilibiliPlaybackAssociationStore
import com.liuguanyi.liumusic.core.provider.spotify.SpotifySessionStore
import com.liuguanyi.liumusic.core.provider.youtubemusic.YouTubeSessionStore
import java.security.MessageDigest

/**
 * Process-local bridge used by Media3's synchronous Resolver callback. No
 * credentials are embedded into MediaItems or custom URIs.
 */
object ProviderPlaybackRuntime {
    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var currentRegistry: MusicProviderRegistry? = null

    fun initialize(context: Context) {
        val application = context.applicationContext
        if (appContext === application && currentRegistry != null) return
        synchronized(this) {
            if (appContext === application && currentRegistry != null) return
            appContext = application
            currentRegistry = LiuMusicMusicProviders.createPlayback(application)
        }
    }

    fun registryOrNull(): MusicProviderRegistry? = currentRegistry

    fun authKey(source: MusicSource): String {
        val context = appContext ?: return ""
        val credential = when (source) {
            MusicSource.Netease -> PlaybackAccountStore.neteaseCookie(context)
            MusicSource.Joox, MusicSource.Higequ -> ""
            MusicSource.QQMusic -> PlaybackAccountStore.qqSession(context).cookie
            MusicSource.Kugou -> PlaybackAccountStore.kugouSession(context).let { session ->
                listOf(session.userId, session.token, session.vipToken, session.dfid).joinToString("|")
            }
            MusicSource.AppleMusic -> AppleMusicSessionStore.read(context).let { session ->
                listOf(session.developerToken, session.musicUserToken, session.storefront).joinToString("|")
            }
            MusicSource.Bilibili -> BilibiliSessionStore.read(context).cookie + "|" +
                BilibiliPlaybackAssociationStore.revision(context)
            MusicSource.Spotify -> SpotifySessionStore.read(context).let { session ->
                listOf(session.accountId, session.expiresAtEpochMs).joinToString("|")
            }
            MusicSource.YouTubeMusic -> YouTubeSessionStore.authFingerprint(context)
            MusicSource.Kuwo -> ""
            MusicSource.Jellyfin -> com.liuguanyi.liumusic.core.provider.jellyfin.JellyfinSessionStore.read(context).let { session ->
                listOf(session.serverUrl, session.accessToken, session.userId).joinToString("|")
            }
            MusicSource.Local -> "local"
        }
        return MessageDigest.getInstance("SHA-256").digest(credential.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
