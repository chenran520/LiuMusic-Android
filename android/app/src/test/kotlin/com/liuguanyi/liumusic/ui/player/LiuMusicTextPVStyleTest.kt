package com.liuguanyi.liumusic.ui.player

import com.liuguanyi.liumusic.ui.settings.LiuMusicTextPVStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class LiuMusicTextPVStyleTest {
    @Test
    fun eighteenPublishedStylesHaveIndependentVisualSignatures() {
        val compatibility = setOf(LiuMusicTextPVStyle.Dynamic, LiuMusicTextPVStyle.Minimal, LiuMusicTextPVStyle.Cyber)
        val published = LiuMusicTextPVStyle.entries.filterNot(compatibility::contains)
        assertEquals(18, published.size)
        assertEquals(18, published.map(LiuMusicTextPVStyle::visualSignature).distinct().size)
    }
}
