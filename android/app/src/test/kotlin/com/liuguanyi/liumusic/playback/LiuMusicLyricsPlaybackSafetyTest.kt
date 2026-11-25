package com.liuguanyi.liumusic.playback

import com.liuguanyi.liumusic.platform.floating.shouldClearLegacyNeteaseLyrics
import com.liuguanyi.liumusic.platform.lyricon.lyriconReloadKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicLyricsPlaybackSafetyTest {
    @Test
    fun queueDurationExtraHasStableKeyAndSafeLegacyFallback() {
        assertEquals("liumusic.track.duration_ms", PlaybackTrackIdentity.DurationMsExtra)
        assertEquals(180_000L, normalizedQueueDurationMs(180_000L))
        assertEquals(0L, normalizedQueueDurationMs(null))
        assertEquals(0L, normalizedQueueDurationMs(-1L))
    }

    @Test
    fun lyriconReloadsWhenDurationBecomesKnownOrAutomaticModeChanges() {
        val unknown = lyriconReloadKey("liumusic:spotify:id", "Title", "Artist", 0L, true)
        val known = lyriconReloadKey("liumusic:spotify:id", "Title", "Artist", 180_000L, true)
        val manual = lyriconReloadKey("liumusic:spotify:id", "Title", "Artist", 180_000L, false)

        assertNotEquals(unknown, known)
        assertNotEquals(known, manual)
        assertEquals(known, lyriconReloadKey("liumusic:spotify:id", "Title", "Artist", 180_001L, true))
        assertNull(lyriconReloadKey(null, "", "", 0L, true))
    }

    @Test
    fun providerIdsKeepSystemLyricsWhileLegacyNeteaseGuardStaysStrict() {
        assertFalse(shouldClearLegacyNeteaseLyrics("123"))
        assertFalse(shouldClearLegacySystemLyrics("123"))
        assertTrue(shouldClearLegacyNeteaseLyrics("liumusic:joox:abc"))
        assertFalse(shouldClearLegacySystemLyrics("liumusic:joox:abc"))
        listOf(null, "", "liumusic:unknown:abc", "not-a-number").forEach { mediaId ->
            assertTrue(shouldClearLegacyNeteaseLyrics(mediaId))
            assertTrue(shouldClearLegacySystemLyrics(mediaId))
        }
    }
}
