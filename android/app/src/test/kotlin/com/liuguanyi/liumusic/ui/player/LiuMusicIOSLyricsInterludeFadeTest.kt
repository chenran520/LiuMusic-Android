package com.liuguanyi.liumusic.ui.player

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicIOSLyricsInterludeFadeTest {
    private val source = File("src/main/kotlin/com/liuguanyi/liumusic/ui/player/LiuMusicIOSLyricsPanel.kt").readText()

    @Test
    fun interludeUsesNormalAnimatedFocusHandoff() {
        val effect = source.substringAfter(
            "LaunchedEffect(colorHighlightedIndex, activeTimedLineIndexes, activeInterludeIndex, document)",
        ).substringBefore("val scrollHideThresholdPx")

        assertTrue(effect.contains("handOffFocusColor(emptySet())"))
        assertFalse(effect.contains("snapTo(0f)"))
    }

    @Test
    fun waitingPresentationDoesNotMaskAnimatedFocus() {
        assertTrue(source.contains("val effectiveFocus = fp"))
        assertFalse(source.contains("lyricsAreWaiting -> 0f"))
    }

    @Test
    fun focusHandoffKeepsSmoothTimingAndReduceMotionBehavior() {
        assertTrue(source.contains("const val FOCUS_COLOR_DURATION_MS = 120"))
        assertTrue(source.contains("if (LiuMusicSettingsRuntime.lyricReduceMotion) anim.snapTo(target)"))
        assertTrue(source.contains("easing = SourceSmoothStepEasing"))
    }
}
