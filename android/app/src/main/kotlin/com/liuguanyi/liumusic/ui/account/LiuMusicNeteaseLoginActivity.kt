package com.liuguanyi.liumusic.ui.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.liuguanyi.liumusic.core.account.rememberNeteaseSessionStore
import com.liuguanyi.liumusic.core.music.provider.PlaybackAccountSlot
import com.liuguanyi.liumusic.ui.LiuMusicPageHost
import com.liuguanyi.liumusic.ui.finishLiuMusicPage
import com.liuguanyi.liumusic.ui.prepareLiuMusicPageWindow
import com.liuguanyi.liumusic.ui.startLiuMusicPage
import com.liuguanyi.liumusic.ui.theme.LiuMusicTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.liuguanyi.liumusic.ui.glass.LocalLiuMusicBackdrop

/**
 * **网易云登录页 —— 一个真实 Activity。**
 *
 * 与 [com.liuguanyi.liumusic.ui.settings.LiuMusicSettingsDetailActivity] 同款：整页推入 / 退回 / 打断 /
 * 预测式返回**全部由平台负责**（不透明窗口 `Theme.LiuMusic.Page` + 不覆盖 `windowAnimationStyle`），
 * 这样登录页的转场就和「设置 → 详情」这一类系统原生页面完全一致。
 *
 * 用户报登录页用的是首页的 `liuMusicPagePushSpec` 弹簧推入（那是首页做整页推入的动画），跟设置页
 * 原生转场不成套 —— 故与设置详情页一样改成独立 Activity。
 *
 * 约定（同 [com.liuguanyi.liumusic.ui.settings.LiuMusicSettingsDetailActivity]）：
 * - 主题走 `Theme.LiuMusic.Page`（**不透明**窗口）—— 半透明窗口在系统里拿不到 ROM 原生的
 *   Activity 转场。
 * - **不要**加 `overridePendingTransition` / `ActivityOptions.makeCustomAnimation`，也不要在主题里
 *   加 `windowAnimationStyle`。
 * - `prepareLiuMusicPageWindow()` 必须在 `setContent` 之前：按 App 真实配色设窗口底色，避免转场第一帧
 *   闪一次错色。
 * - 本页用 `rememberLayerBackdrop()` + `layerBackdrop` 自建玻璃背板，并用 `LocalLiuMusicBackdrop`
 *   注入整棵组合树 —— 否则 `LiuMusicGlassIconButton`（返回键）没有可模糊的背板，会退化成死板图标，
 *   与首页 / 列表页的液态返回键不同款。
 *
 * 登录结果通过 `Activity.setResult` 回传：成功 `RESULT_OK`、取消 `RESULT_CANCELED`，由调用方
 * （首页等）据此决定是否切到回跳 tab。
 */
class LiuMusicNeteaseLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetSlot = intent.getStringExtra(EXTRA_TARGET_SLOT)
            ?.let { name -> runCatching { PlaybackAccountSlot.valueOf(name) }.getOrNull() }
            ?: PlaybackAccountSlot.Main
        enableEdgeToEdge()
        prepareLiuMusicPageWindow()
        setContent {
            LiuMusicPageHost {
                LiuMusicTheme {
                    val pageBackdrop = rememberLayerBackdrop()
                    Box(Modifier.fillMaxSize()) {
                        // 玻璃背板捕获层：提供「可被模糊」的背景，供返回键等玻璃件采样。
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .layerBackdrop(pageBackdrop),
                        )
                        CompositionLocalProvider(LocalLiuMusicBackdrop provides pageBackdrop) {
                            NeteaseLoginScreen(
                                session = rememberNeteaseSessionStore(),
                                onDismiss = ::finishLiuMusicPage,
                                onLoggedIn = {
                                    setResult(RESULT_OK)
                                    finishLiuMusicPage()
                                },
                                targetSlot = targetSlot,
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_TARGET_SLOT = "target_slot"

        /** 构造登录页 Intent（供 startActivityForResult 取登录结果用）。 */
        fun createIntent(
            context: Context,
            targetSlot: PlaybackAccountSlot = PlaybackAccountSlot.Main,
        ): Intent =
            Intent(context, LiuMusicNeteaseLoginActivity::class.java)
                .putExtra(EXTRA_TARGET_SLOT, targetSlot.name)
                .apply { if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        /** 由首页 / 设置 / 服务页的登录入口调用（无需取结果时）。 */
        fun launch(
            context: Context,
            targetSlot: PlaybackAccountSlot = PlaybackAccountSlot.Main,
        ) {
            context.startLiuMusicPage(createIntent(context, targetSlot))
        }
    }
}
