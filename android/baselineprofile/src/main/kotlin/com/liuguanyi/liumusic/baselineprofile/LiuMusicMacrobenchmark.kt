package com.liuguanyi.liumusic.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiuMusicMacrobenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = rule.measureRepeated(
        packageName = LiuMusicPackage,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.COLD,
        iterations = 8,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
    }

    @Test
    fun settingsAndPlayerFrames() = rule.measureRepeated(
        packageName = LiuMusicPackage,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        iterations = 5,
        setupBlock = { pressHome() },
    ) {
        val device = startLiuMusic()
        device.openSettingsAndScroll()
        device.exercisePlayerIfAvailable()
    }
}
