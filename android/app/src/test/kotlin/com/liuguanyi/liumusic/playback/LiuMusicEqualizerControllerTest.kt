package com.liuguanyi.liumusic.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class LiuMusicEqualizerControllerTest {
    @Test
    fun physicalBandsUseLogFrequencyInterpolation() {
        val gains = List(10) { it.toFloat() }

        assertEquals(0f, LiuMusicEqualizerController.interpolatedGain(20f, gains), 0.001f)
        assertEquals(5f, LiuMusicEqualizerController.interpolatedGain(1_000f, gains), 0.001f)
        assertEquals(9f, LiuMusicEqualizerController.interpolatedGain(20_000f, gains), 0.001f)
        assertEquals(5.5f, LiuMusicEqualizerController.interpolatedGain(1_414.2135f, gains), 0.02f)
    }
}
