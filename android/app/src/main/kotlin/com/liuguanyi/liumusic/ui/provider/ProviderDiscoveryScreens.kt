package com.liuguanyi.liumusic.ui.provider

import androidx.compose.runtime.Composable
import com.liuguanyi.liumusic.core.account.rememberNeteaseSessionStore
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.ui.discovery.LiuMusicExploreScreen
import com.liuguanyi.liumusic.ui.discovery.LiuMusicHomeScreen
import com.liuguanyi.liumusic.ui.library.LibraryScreen

/**
 * Compatibility entry points while LiuMusicApp still imports provider-prefixed names.
 *
 * These functions intentionally contain no provider presentation. Provider
 * differences end at the data/capability bridge; all animation, liquid-glass,
 * flowing-light backgrounds and future LiuMusic/iOS UI migrations live in the
 * canonical LiuMusic screens.
 */
@Composable
fun ProviderHomeScreen(source: MusicSource) = LiuMusicHomeScreen(source)

@Composable
fun ProviderExploreScreen(source: MusicSource) = LiuMusicExploreScreen(source)

@Composable
fun ProviderLibraryScreen(source: MusicSource) {
    LibraryScreen(
        session = rememberNeteaseSessionStore(),
        onLogin = null,
        source = source,
    )
}
