package com.liuguanyi.liumusic.core.lyrics

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiuMusicLyricPreferenceTest {

    @Test
    fun settingOverridesTheSystemLocale() {
        val system = Locale.forLanguageTag("zh-Hans-CN")

        val explicitTurkish = LiuMusicLyricPreference.fromSetting("tr", system)
        assertEquals("tr", explicitTurkish.languageTag)
        assertEquals(LiuMusicLyricScript.Original, explicitTurkish.script)

        val explicitTraditional = LiuMusicLyricPreference.fromSetting("zh-Hant", system)
        assertNull(explicitTraditional.languageTag)
        assertEquals(LiuMusicLyricScript.Traditional, explicitTraditional.script)

        val explicitSimplified = LiuMusicLyricPreference.fromSetting("zh-Hans", system)
        assertNull(explicitSimplified.languageTag)
        assertEquals(LiuMusicLyricScript.Simplified, explicitSimplified.script)
    }

    @Test
    fun systemSettingFollowsTheLocale() {
        val turkish = LiuMusicLyricPreference.fromSetting("system", Locale.forLanguageTag("tr-TR"))
        assertEquals("tr", turkish.languageTag)

        val traditionalChinese = LiuMusicLyricPreference.fromSetting("system", Locale.forLanguageTag("zh-Hant-TW"))
        assertNull(traditionalChinese.languageTag)
        assertEquals(LiuMusicLyricScript.Traditional, traditionalChinese.script)
    }

    @Test
    fun unknownOrBlankSettingsFallBackToTheSystemLocale() {
        val system = Locale.forLanguageTag("en-US")
        assertEquals(LiuMusicLyricPreference.system(system), LiuMusicLyricPreference.fromSetting("", system))
        assertEquals(LiuMusicLyricPreference.system(system), LiuMusicLyricPreference.fromSetting("   ", system))
        // 地区标签按基础语言匹配，这样 en-US 也能命中 en 轨
        assertEquals("en", LiuMusicLyricPreference.fromSetting("en-US", system).languageTag)
    }
}
