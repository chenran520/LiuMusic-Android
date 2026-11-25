package com.liuguanyi.liumusic.playback

import com.liuguanyi.liumusic.core.audio.MusicQuality
import com.liuguanyi.liumusic.core.audio.MusicQualityRuntime
import com.liuguanyi.liumusic.core.music.model.AudioQualityTier
import com.liuguanyi.liumusic.core.music.model.MusicResourceId
import com.liuguanyi.liumusic.core.music.model.MusicSource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class ProviderPlaybackQualityRuntimeTest {
    @After
    fun resetRuntime() {
        MusicQualityRuntime.selected = MusicQuality.Standard
        ProviderPlaybackQualityRuntime.clear()
    }

    @Test
    fun standardBackgroundAnalysisDoesNotReplaceProviderHiResQuality() {
        val id = MusicResourceId(MusicSource.Kugou, "track-hash")
        MusicQualityRuntime.selected = MusicQuality.HiResolution
        ProviderPlaybackQualityRuntime.recordActual(
            id = id,
            requested = AudioQualityTier.HiResolution,
            actual = AudioQualityTier.HiResolution,
        )

        ProviderPlaybackQualityRuntime.recordActual(
            id = id,
            requested = AudioQualityTier.Standard,
            actual = AudioQualityTier.Standard,
        )

        assertEquals(AudioQualityTier.HiResolution, ProviderPlaybackQualityRuntime.actualFor(id))
    }
}
