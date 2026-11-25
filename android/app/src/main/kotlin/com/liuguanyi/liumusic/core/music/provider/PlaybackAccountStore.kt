package com.liuguanyi.liumusic.core.music.provider

import android.content.Context
import com.liuguanyi.liumusic.core.account.NeteaseSessionStore
import com.liuguanyi.liumusic.core.provider.kugou.KugouSession
import com.liuguanyi.liumusic.core.provider.kugou.KugouSessionStore
import com.liuguanyi.liumusic.core.provider.kuwo.KuwoSession
import com.liuguanyi.liumusic.core.provider.kuwo.KuwoSessionStore
import com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicSession
import com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicSessionStore

object PlaybackAccountStore {
    private const val PreferencesName = "liumusic_playback_account"
    private const val Enabled = "enabled"

    fun isEnabled(context: Context): Boolean = context.applicationContext
        .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE).getBoolean(Enabled, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit().putBoolean(Enabled, enabled).apply()
        if (!enabled) clear(context)
    }

    fun neteaseCookie(context: Context): String = selectPlaybackSession(
        isEnabled(context), NeteaseSessionStore.readPlaybackCookie(context), NeteaseSessionStore.readCookie(context), NeteaseSessionStore::containsMusicU,
    )

    fun qqSession(context: Context): QQMusicSession = selectPlaybackSession(
        isEnabled(context), QQMusicSessionStore.read(context, playback = true), QQMusicSessionStore.read(context), QQMusicSession::isLoggedIn,
    )

    fun kugouSession(context: Context): KugouSession = selectPlaybackSession(
        isEnabled(context), KugouSessionStore.read(context, playback = true), KugouSessionStore.read(context), KugouSession::isLoggedIn,
    )

    fun kuwoSession(context: Context): KuwoSession = selectPlaybackSession(
        isEnabled(context), KuwoSessionStore.read(context, playback = true), KuwoSessionStore.read(context), KuwoSession::isLoggedIn,
    )

    fun clear(context: Context) {
        NeteaseSessionStore.clearPlayback(context)
        QQMusicSessionStore.clear(context, clearWebCookies = false, playback = true)
        KugouSessionStore.clearLogin(context, playback = true)
        KuwoSessionStore.clear(context, playback = true)
    }
}

internal fun <T> selectPlaybackSession(enabled: Boolean, playback: T, main: T, isValid: (T) -> Boolean): T =
    playback.takeIf { enabled && isValid(it) } ?: main
