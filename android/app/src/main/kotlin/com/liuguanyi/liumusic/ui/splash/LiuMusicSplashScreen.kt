package com.liuguanyi.liumusic.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuguanyi.liumusic.BuildConfig
import com.liuguanyi.liumusic.R
import kotlinx.coroutines.delay

/**
 * 开屏页。
 *
 * 冷启动时覆盖在首页之上，先只显示应用图标，随后图标下方淡入应用名与作者信息，
 * 让「这是谁做的」在第一时间被看到。
 *
 * 只读显示，不承担任何初始化职责 —— 初始化仍在 MainActivity / LiuMusicApp 各自的
 * 生命周期协程里，避免开屏时长与冷启动耗时互相绑架。
 *
 * @param visible 是否显示。false 时整体 alpha 归零，由调用方在动画结束后移除。
 * @param onFinished 淡出动画结束后的回调（可选）。
 */
@Composable
fun LiuMusicSplashScreen(
    visible: Boolean,
    onFinished: (() -> Unit)? = null,
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(360),
        label = "liumusic-splash-alpha",
        finishedListener = { if (!visible) onFinished?.invoke() },
    )

    // 图标先出现，文字稍后淡入 —— 避免开屏一瞬间文字和图标同时砸出来。
    var detailVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(180L)
        detailVisible = true
    }
    val detailAlpha by animateFloatAsState(
        targetValue = if (detailVisible && visible) 1f else 0f,
        animationSpec = tween(420),
        label = "liumusic-splash-detail-alpha",
    )

    if (alpha <= 0.01f) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // 应用图标
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(26.dp)),
            )

            Spacer(Modifier.height(22.dp))

            // 应用名
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = SplashPrimaryText,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(10.dp))

            // 副标题
            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 13.sp,
                color = SplashSecondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            // 作者信息 —— 开屏页里最该被看见的东西
            Spacer(Modifier.height(34.dp))
            Box(
                modifier = Modifier
                    .alpha(detailAlpha)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SplashAuthorCard)
                    .padding(horizontal = 26.dp, vertical = 16.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.splash_author_label),
                        fontSize = 11.sp,
                        color = SplashAuthorLabelText,
                        letterSpacing = 3.sp,
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = stringResource(R.string.splash_author_name),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SplashAuthorNameText,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = stringResource(R.string.splash_author_email),
                        fontSize = 13.sp,
                        color = SplashAuthorEmailText,
                    )
                }
            }
        }

        // 底部版本信息
        Text(
            text = stringResource(R.string.splash_version, BuildConfig.VERSION_NAME),
            fontSize = 11.sp,
            color = SplashVersionText,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(detailAlpha),
        )
    }
}

// 开屏页固定使用品牌色，不跟随 App 内的浅色/深色切换 ——
// 开屏是「门面」，配色必须始终一致。
private val SplashBackground = Color(0xFFFFFFFF)
private val SplashPrimaryText = Color(0xFF1C1C1E)
private val SplashSecondaryText = Color(0xFF8E8E93)
private val SplashAuthorCard = Color(0xFFFDECEF)
private val SplashAuthorLabelText = Color(0xFFB06070)
private val SplashAuthorNameText = Color(0xFFC2185B)
private val SplashAuthorEmailText = Color(0xFF7A5C64)
private val SplashVersionText = Color(0xFFC7C7CC)
