package com.liuguanyi.liumusic.core.lyrics

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LiuMusicLyricScriptConverterTest {
    @Test
    fun followsSystemChineseScript() {
        assertEquals(LiuMusicLyricScript.Traditional, LiuMusicLyricScript.fromSystem(Locale.TAIWAN))
        assertEquals(LiuMusicLyricScript.Simplified, LiuMusicLyricScript.fromSystem(Locale.CHINA))
        assertEquals(LiuMusicLyricScript.Original, LiuMusicLyricScript.fromSystem(Locale.ENGLISH))
    }

    @Test
    fun originalScriptLeavesLyricsUnchanged() {
        val document = LyricsDocument(listOf(LyricLine(0L, text = "愛", translation = "爱")))
        assertEquals(document, LiuMusicLyricScriptConverter.convert(document, LiuMusicLyricScript.Original))
    }
}
