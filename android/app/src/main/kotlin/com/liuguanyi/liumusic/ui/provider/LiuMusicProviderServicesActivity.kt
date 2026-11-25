package com.liuguanyi.liumusic.ui.provider

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.liuguanyi.liumusic.core.account.rememberNeteaseSessionStore
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.core.music.provider.MusicProviderSelectionStore
import com.liuguanyi.liumusic.ui.prepareLiuMusicPageWindow
import com.liuguanyi.liumusic.ui.theme.LiuMusicTheme
import com.liuguanyi.liumusic.ui.LiuMusicPageHost

class LiuMusicProviderServicesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prepareLiuMusicPageWindow()
        setContent {
            LiuMusicPageHost {
                LiuMusicTheme {
                    ProviderServicesPage()
                }
            }
        }
    }

    @Composable
    private fun ProviderServicesPage() {
        val context = this@LiuMusicProviderServicesActivity
        val session = rememberNeteaseSessionStore()
        var currentSource by remember { mutableStateOf(MusicProviderSelectionStore.selectedSource(context)) }
        ProviderServicesScreen(
            currentSource = currentSource,
            onSourceSelected = { source ->
                currentSource = source
                MusicProviderSelectionStore.setSelectedSource(context, source)
            },
            neteaseSession = session,
            onNeteaseLogin = {
                startActivity(Intent(context, com.liuguanyi.liumusic.ui.account.LiuMusicNeteaseLoginActivity::class.java))
            },
            onBack = ::finish,
        )
    }

    companion object {
        fun launch(context: Context) {
            context.startActivity(
                Intent(context, LiuMusicProviderServicesActivity::class.java)
                    .apply { if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
            )
        }
    }
}
