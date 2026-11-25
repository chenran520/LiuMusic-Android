package com.liuguanyi.liumusic.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = LiuMusicPackage,
        includeInStartupProfile = true,
    ) {
        val device = startLiuMusic()
        device.openSettingsAndScroll()
        device.exercisePlayerIfAvailable()
    }
}
