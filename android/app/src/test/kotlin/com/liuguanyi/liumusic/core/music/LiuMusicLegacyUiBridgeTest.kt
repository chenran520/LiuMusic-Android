package com.liuguanyi.liumusic.core.music

import com.liuguanyi.liumusic.core.music.model.MusicArtistRef
import com.liuguanyi.liumusic.core.music.model.MusicPlaylistSummary
import com.liuguanyi.liumusic.core.music.model.MusicResourceId
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.core.music.model.MusicTrack
import com.liuguanyi.liumusic.core.music.provider.LiuMusicLegacyUiBridge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicLegacyUiBridgeTest {
    @Test
    fun providerTrackKeepsNativeIdentityWhileUsingStableUiId() {
        val track = MusicTrack(
            id = MusicResourceId(MusicSource.QQMusic, "0039MnYb0qxYhV"),
            title = "Test Song",
            artists = listOf(MusicArtistRef(name = "Test Artist")),
            durationMs = 123_000L,
        )

        val first = LiuMusicLegacyUiBridge.track(track)
        val second = LiuMusicLegacyUiBridge.track(track)

        assertTrue(first.id < 0L)
        assertEquals(first.id, second.id)
        assertEquals(track.id, first.providerTrack?.id)
        assertSame(track, first.providerTrack)
        assertEquals("Test Song", first.name)
        assertEquals("Test Artist", first.artists)
    }

    @Test
    fun providerPlaylistKeepsNativeIdentityForCanonicalLibraryRenderer() {
        val playlist = MusicPlaylistSummary(
            id = MusicResourceId(MusicSource.Kugou, "playlist-native-id"),
            title = "Test Playlist",
            artworkUrl = "https://example.invalid/cover.jpg",
            creatorName = "Creator",
            trackCount = 42,
            playCount = 1234L,
        )

        val bridged = LiuMusicLegacyUiBridge.playlist(playlist)

        assertTrue(bridged.id < 0L)
        assertSame(playlist, bridged.providerPlaylist)
        assertEquals(playlist.id, bridged.providerPlaylist?.id)
        assertEquals("Test Playlist", bridged.name)
        assertEquals(42, bridged.trackCount)
    }

    @Test
    fun providerLibraryMapsIntoExistingLiuMusicSnapshotWithoutInventingFeatures() {
        val playlist = MusicPlaylistSummary(
            id = MusicResourceId(MusicSource.QQMusic, "playlist-1"),
            title = "Library Playlist",
        )

        val snapshot = LiuMusicLegacyUiBridge.library(listOf(playlist))

        assertEquals(1, snapshot.playlists.size)
        assertEquals(playlist.id, snapshot.playlists.single().providerPlaylist?.id)
        assertTrue(snapshot.likedSongs.isEmpty())
        assertTrue(snapshot.recentSongs.isEmpty())
        assertEquals(null, snapshot.likedPlaylistId)
    }
}
