package com.liuguanyi.liumusic.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
 * 视觉层级刻意做成「图标 + 作者」双主角：
 * 上半是应用图标与应用名，下半是一张整幅宽的渐变大卡片，卡片里是放大的作者署名，
 * 让「这是谁做的」在开屏第一眼就被看到，而不是缩在角落的小字。
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
    // 关键：alpha 的初值必须跟 visible 走。
    // 如果这里写成 animateFloatAsState(targetValue = if (visible) 1f else 0f)，
    // 首帧 targetValue 就是 1f，动画认为「已经到位」不回调节点，
    // 下面的 alpha <= 0.01f 早退判断虽然不会误伤，但 finishedListener 永远不会触发，
    // 开屏页就再也不会被移除。用 initialState 显式固定起点，淡出才有终点事件。
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(360),
        label = "liumusic-splash-alpha",
        finishedListener = { final ->
            // 只有「淡出到 0」才通知外层卸载。
            if (!visible && final <= 0.01f) onFinished?.invoke()
        },
    )

    // 图标先出现，作者卡片稍后淡入 —— 避免开屏一瞬间所有元素同时砸出来。
    var detailVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(160L)
        detailVisible = true
    }
    val detailAlpha by animateFloatAsState(
        targetValue = if (detailVisible && visible) 1f else 0f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "liumusic-splash-detail-alpha",
    )
    // 卡片淡入时轻微上浮，视线自然被带到作者信息上。
    val detailLift by animateFloatAsState(
        targetValue = if (detailVisible && visible) 0f else 18f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "liumusic-splash-detail-lift",
    )

    // 作者姓名的极轻微呼吸缩放（0.985 ↔ 1.0），让这张卡片成为画面焦点。
    val breathe = rememberInfiniteTransition(label = "liumusic-splash-breathe")
    val breathScale by breathe.animateFloat(
        initialValue = 0.985f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liumusic-splash-breath",
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
        ) {
            // ── 应用图标 ──
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(26.dp)),
            )

            Spacer(Modifier.height(18.dp))

            // ── 应用名 ──
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = SplashPrimaryText,
                letterSpacing = 3.sp,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 12.5.sp,
                color = SplashSecondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
            )

            Spacer(Modifier.height(30.dp))

            // ── 作者信息：整幅宽渐变大卡片，开屏页的第二个视觉主角 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(detailAlpha)
                    .offset(y = detailLift.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SplashAuthorCardStart, SplashAuthorCardEnd),
                        ),
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(SplashAuthorBorderStart, SplashAuthorBorderEnd),
                        ),
                        shape = RoundedCornerShape(26.dp),
                    )
                    .padding(horizontal = 22.dp, vertical = 22.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // 头像圆环 + 姓氏
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SplashAvatarStart, SplashAvatarEnd),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.splash_author_avatar),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Spacer(Modifier.height(13.dp))

                    Text(
                        text = stringResource(R.string.splash_author_label),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = SplashAuthorLabelText,
                        letterSpacing = 5.sp,
                    )

                    Spacer(Modifier.height(9.dp))

                    // 作者姓名 —— 开屏页里字号仅次于应用名的元素
                    Text(
                        text = stringResource(R.string.splash_author_name),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = SplashAuthorNameText,
                        letterSpacing = 6.sp,
                        modifier = Modifier.scale(breathScale),
                    )

                    Spacer(Modifier.height(12.dp))

                    // 分隔线
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(SplashAuthorDivider),
                    )

                    Spacer(Modifier.height(12.dp))

                    // 邮箱：加底色胶囊，可读性拉满
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SplashAuthorEmailPill)
                            .padding(horizontal = 16.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.splash_author_email),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SplashAuthorEmailText,
                        )
                    }
                }
            }
        }

        // ── 底部：版权 + 版本 ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
                .alpha(detailAlpha),
        ) {
            Text(
                text = stringResource(R.string.splash_copyright),
                fontSize = 11.sp,
                color = SplashVersionText,
                letterSpacing = 0.4.sp,
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(SplashVersionText),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.splash_version, BuildConfig.VERSION_NAME),
                fontSize = 11.sp,
                color = SplashVersionText,
            )
        }
    }
}


// 开屏页固定使用品牌色，不跟随 App 内的浅色/深色切换 ——
// 开屏是「门面」，配色必须始终一致。
private val SplashBackground = Color(0xFFFFFFFF)
private val SplashPrimaryText = Color(0xFF1C1C1E)
private val SplashSecondaryText = Color(0xFF8E8E93)
private val SplashVersionText = Color(0xFFC7C7CC)

// 作者卡片：从白粉过渡到品牌品红，让它在纯白背景上「跳」出来。
private val SplashAuthorCardStart = Color(0xFFFFF1F4)
private val SplashAuthorCardEnd = Color(0xFFFDDBE4)
private val SplashAuthorBorderStart = Color(0xFFFFC2D1)
private val SplashAuthorBorderEnd = Color(0xFFF48FB1)
private val SplashAvatarStart = Color(0xFFEC407A)
private val SplashAvatarEnd = Color(0xFFC2185B)
private val SplashAuthorLabelText = Color(0xFFAD1457)
private val SplashAuthorNameText = Color(0xFFB0003A)
private val SplashAuthorDivider = Color(0x33C2185B)
private val SplashAuthorEmailPill = Color(0x1FC2185B)
private val SplashAuthorEmailText = Color(0xFF8E0038)
