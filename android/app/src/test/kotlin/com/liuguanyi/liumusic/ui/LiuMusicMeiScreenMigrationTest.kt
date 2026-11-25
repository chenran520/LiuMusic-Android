package com.liuguanyi.liumusic.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicMeiScreenMigrationTest {
    @Test
    fun albumAndArtistRoutesUseMigratedScreens() {
        val activity = File("src/main/kotlin/com/liuguanyi/liumusic/ui/collection/LiuMusicCollectionDetailActivity.kt").readText()
        val library = File("src/main/kotlin/com/liuguanyi/liumusic/ui/library/LibraryScreen.kt").readText()

        assertTrue(activity.contains("LiuMusicUnifiedAlbumDetailScreen(id, ::finish)"))
        assertTrue(activity.contains("LiuMusicArtistDetailScreen(id, onExit)"))
        assertTrue(library.contains("internal fun LiuMusicUnifiedProviderAlbumDetailScreen"))
    }

    @Test
    fun searchSongRowsUsePlaylistSwipeActions() {
        val search = File("src/main/kotlin/com/liuguanyi/liumusic/ui/search/SearchScreen.kt").readText()

        // 文案已资源化：这里断言的是行为接线与所用资源，而不是中文字面量。
        assertTrue(search.contains("private fun SearchSwipeSongRow"))
        assertTrue(search.split("LiuMusicSwipeAction(").size >= 4)
        assertTrue(search.contains("R.string.player_play_next"))
        assertTrue(search.contains("R.string.artist_play_later"))
        assertTrue(search.contains("R.string.artist_add_library"))
    }

    @Test
    fun songWikiAccountAndArtistPagesAreWired() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val actions = File("src/main/kotlin/com/liuguanyi/liumusic/ui/player/LiuMusicSongActionsOverlay.kt").readText()
        val account = File("src/main/kotlin/com/liuguanyi/liumusic/ui/account/LiuMusicAccountActivity.kt").readText()
        val artist = File("src/main/kotlin/com/liuguanyi/liumusic/ui/collection/LiuMusicArtistDetailScreen.kt").readText()

        assertTrue(manifest.contains(".ui.song.LiuMusicSongWikiActivity"))
        assertTrue(actions.contains("LiuMusicSongWikiActivity.launch(context, song)"))
        assertTrue(account.contains("LiuMusicGlassSegmentedControl"))
        assertTrue(account.contains("AccountHero"))
        assertTrue(artist.contains("ArtistHero"))
        assertTrue(artist.contains("LazyRow"))
    }

    @Test
    fun podcastUsesMeiLayoutAndSharedDirectRoute() {
        val podcast = File("src/main/kotlin/com/liuguanyi/liumusic/ui/podcast/LiuMusicPodcastScreens.kt").readText()
        val activity = File("src/main/kotlin/com/liuguanyi/liumusic/ui/collection/LiuMusicCollectionDetailActivity.kt").readText()

        assertTrue(podcast.contains("initialPodcastId: Long? = null"))
        assertTrue(podcast.contains("PodcastCategoryButton"))
        assertTrue(podcast.contains("LiuMusicPinnedListPage("))
        assertTrue(podcast.contains("PlaybackCommands.playQueue(context, songs, selected.id)"))
        assertTrue(activity.contains("LiuMusicPodcastScreen("))
    }

    @Test
    fun themeProvidesDarkAwareRootContentColor() {
        val theme = File("src/main/kotlin/com/liuguanyi/liumusic/ui/theme/LiuMusicTheme.kt").readText()

        assertTrue(theme.contains("onSurfaceVariant = Color(0xFFB8B8C0)"))
        assertTrue(theme.contains("contentColor = MaterialTheme.colorScheme.onBackground"))
    }
}
