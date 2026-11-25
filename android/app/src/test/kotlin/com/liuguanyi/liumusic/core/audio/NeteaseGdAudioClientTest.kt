package com.liuguanyi.liumusic.core.audio

import com.liuguanyi.liumusic.core.music.model.AudioQualityTier
import org.junit.Assert.assertEquals
import org.junit.Test

class NeteaseGdAudioClientTest {
    @Test
    fun gdBitrateLadderMatchesAuroraSourcePolicy() {
        assertEquals(listOf(192, 128), NeteaseGdAudioClient.bitrateCandidates(192))
        assertEquals(listOf(320, 128), NeteaseGdAudioClient.bitrateCandidates(320))
        assertEquals(listOf(320, 128), NeteaseGdAudioClient.bitrateCandidates(999))
    }

    @Test
    fun LiuMusicQualitySettingsMapToGdBitrates() {
        assertEquals(192, NeteaseGdAudioClient.bitrateFor(AudioQualityTier.Standard))
        assertEquals(320, NeteaseGdAudioClient.bitrateFor(AudioQualityTier.High))
        assertEquals(999, NeteaseGdAudioClient.bitrateFor(AudioQualityTier.Lossless))
        assertEquals(999, NeteaseGdAudioClient.bitrateFor(MusicQuality.HiResolution))
    }
}
