package com.liuguanyi.liumusic.ui.discovery

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicHomeFullBleedTest {
    @Test
    fun homeViewportIsFullWidthAndInsetsIndividualBlocks() {
        val source = File("src/main/kotlin/com/liuguanyi/liumusic/ui/discovery/LiuMusicDiscoveryScreens.kt").readText()

        assertFalse(source.contains(".widthIn(max = window.maxContentWidth)"))
        assertTrue(source.contains("contentPadding = PaddingValues(top = 18.dp + statusBarTop, bottom = 146.dp)"))
        assertTrue(source.contains("contentPadding = PaddingValues(horizontal = 20.dp)"))
        assertTrue(source.contains("SectionTitle(block.title, block.trailing, Modifier.padding(horizontal = 20.dp))"))
    }
}
