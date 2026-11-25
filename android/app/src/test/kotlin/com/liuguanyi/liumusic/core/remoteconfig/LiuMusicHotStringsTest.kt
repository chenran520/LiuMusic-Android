package com.liuguanyi.liumusic.core.remoteconfig

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiuMusicHotStringsTest {

    @After
    fun tearDown() {
        LiuMusicHotStrings.update(emptyMap())
    }

    @Test
    fun exactLocaleWinsThenLanguageThenDefault() {
        LiuMusicHotStrings.update(
            mapOf(
                "zh-CN" to mapOf("a" to "cn"),
                "zh" to mapOf("a" to "zh", "b" to "zh-b"),
                "default" to mapOf("a" to "def", "c" to "def-c"),
            ),
        )

        assertEquals("cn", LiuMusicHotStrings.override("zh-CN", "a"))
        assertEquals("zh-b", LiuMusicHotStrings.override("zh-CN", "b"))
        assertEquals("def-c", LiuMusicHotStrings.override("zh-CN", "c"))
        assertNull(LiuMusicHotStrings.override("zh-CN", "missing"))
    }

    @Test
    fun noOverridesMeansNoReplacement() {
        LiuMusicHotStrings.update(emptyMap())
        assertNull(LiuMusicHotStrings.override("zh-CN", "a"))
    }

    @Test
    fun blankKeyNeverMatches() {
        LiuMusicHotStrings.update(mapOf("default" to mapOf("" to "x")))
        assertNull(LiuMusicHotStrings.override("en", ""))
    }
}
