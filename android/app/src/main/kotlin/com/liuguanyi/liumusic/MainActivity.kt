package com.liuguanyi.liumusic

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.OrientationEventListener
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.liuguanyi.liumusic.core.network.parseNeteaseListenTogetherInvitation
import com.liuguanyi.liumusic.core.remoteconfig.LiuMusicRemoteConfigRuntime
import com.liuguanyi.liumusic.core.remoteconfig.LiuMusicRemoteConfigConsent
import com.liuguanyi.liumusic.platform.LiuMusicVolumeKeyBridge
import com.liuguanyi.liumusic.platform.lyricon.LiuMusicLyriconBridge
import com.liuguanyi.liumusic.platform.xiaomi.HyperOsFocusBridge
import com.liuguanyi.liumusic.ui.player.LiuMusicListenTogetherInviteActivity
import com.liuguanyi.liumusic.ui.LiuMusicApp
import com.liuguanyi.liumusic.ui.settings.LiuMusicSettingsPreferences
import com.liuguanyi.liumusic.ui.splash.LiuMusicSplashScreen
import com.liuguanyi.liumusic.ui.theme.LiuMusicTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var openNowPlayingRequest by mutableIntStateOf(0)
    private var clipboardLinkRequest by mutableStateOf<String?>(null)
    private var playbackConnectionEnabled by mutableStateOf(false)
    private var lastClipboardText: String? = null
    private val firstDraw = CompletableDeferred<Unit>()
    private var clipboardScanJob: Job? = null
    private var handledRemoteConfigForegroundSession = -1L

    // ⚠ 传感器提前升帧（2026-10-09）：onConfigurationChanged 触发时旋转动画已开始，
    //   VRR 升帧赶不上动画开头几帧（录屏实测 frame 58-59 间隔 21.7/11.7ms，
    //   后续才归正到 7-8ms）= 用户报的「一顿一顿」。用 OrientationEventListener
    //   监听传感器角度变化（UI 延迟 ~50ms），比配置变更早 50-100ms 触发
    //   markActive ⇒ 旋转动画全程 120Hz。
    // ⚠ 不能在类字段初始化中创建（会引用 applicationContext，而 ContextWrapper
    //   此时尚未完全构造 → NPE 崩溃 178 版重现）。改在 onCreate 里建。
    private var orientationSensor: OrientationEventListener? = null

    // ⚠ 可变刷新率**不再**在这里挂载：已由 `LiuMusicApplication` 注册的 `LiuMusicFrameRateHost`
    //   统一接管全部 8 个 Activity（见该类注释）。此处若再建一个 controller，
    //   两个实例会同时向同一窗口重复下发帧率请求。

    // ⚠ 旋转升帧（2026-10-09）：旋转是传感器触发、无触摸事件，高刷窗口可能早已过期。
    //   方向变化时手动 markActive()，让旋转动画全程高刷。否则「旋转飞入」会掉帧卡顿。
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        com.liuguanyi.liumusic.core.display.LiuMusicFrameRateHost.onConfigurationChanged(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 传感器监听器在此处创建（不能在类字段初始化时建 —— ContextWrapper
        // 尚未完全构造，applicationContext 返回 null ⇒ 178 版重现的 NPE 崩溃）。
        orientationSensor = object : OrientationEventListener(
            applicationContext, SensorManager.SENSOR_DELAY_UI
        ) {
            private var lastAngle = ORIENTATION_UNKNOWN
            override fun onOrientationChanged(angle: Int) {
                if (angle == ORIENTATION_UNKNOWN) {
                    lastAngle = ORIENTATION_UNKNOWN
                    return
                }
                val prev = lastAngle
                lastAngle = angle
                // 角度突变 >45°：手机正被旋转。UI 延迟事件可能拆成两次小突变，
                // 阈值 45 既过滤手抖又能捕捉 90°/180° 的真实旋转。
                if (prev != ORIENTATION_UNKNOWN && Math.abs(angle - prev) > 45) {
                    com.liuguanyi.liumusic.core.display.LiuMusicFrameRateHost.onConfigurationChanged(
                        this@MainActivity
                    )
                }
            }
        }
        // ⚠ 不在此处禁系统旋转动画（2026-10-09）：JUMPCUT 确实去掉了「系统旋转 + App
        //   orientTransition」双动画叠加，但代价是整个「旋转飞入」观感也一起没了。
        //   卡顿的真凶不是动画叠加，而是旋转时无触摸事件 ⇒ 高刷窗口过期（见
        //   LiuMusicFrameRateHost.onActivityConfigurationChanged 的 markActive）。
        val coldStart = savedInstanceState == null
        // ⚠ 开屏页只在「进程真正的冷启动」时显示。
        //
        // 原实现用「上次退到后台至今是否在 15 分钟内」来跳过开屏页，目的是让用户切出去
        // 接个电话再回来时不必重看一遍。但这个判断有个致命副作用：覆盖安装后
        // SharedPreferences 是保留的，如果装完立刻打开、而上一次退到后台还在 15 分钟内，
        // returnFromBackground 会误判为 true，开屏页直接被跳过 —— 表现为「开屏页没了」。
        //
        // 改为按进程实例判定：Application 每次冷启动都是新进程，用它记录的启动时间戳，
        // 只有在同一进程内（Activity 重建、旋屏）才跳过，跨进程一律显示。
        val returnFromBackground = coldStart && LiuMusicAppVisibility.isSameProcessSession(this)
        LiuMusicAppVisibility.markActivityCreated()
        enableEdgeToEdge()
        consumePlaybackIntent(intent)
        LiuMusicSettingsPreferences.initializeCritical(this)
        completeAfterFirstDraw()

        setContent {
            LiuMusicTheme {
                // 开屏页必须在首帧就存在。用 remember 的初值决定，而不是等 LaunchedEffect
                // 之后再挂载 —— 否则冷启动那几百毫秒里首页会先露出来，开屏页像是「没出现」。
                var splashVisible by remember { mutableStateOf(coldStart && !returnFromBackground) }
                var splashMounted by remember { mutableStateOf(splashVisible) }
                val pageAlpha by animateFloatAsState(
                    targetValue = if (splashVisible) 0f else 1f,
                    animationSpec = tween(350),
                    label = "liumusic-start-page",
                )
                LaunchedEffect(coldStart, returnFromBackground) {
                    if (coldStart && !returnFromBackground) {
                        // 开屏页要读完图标 + 应用名 + 作者信息。
                        delay(SPLASH_HOLD_MS)
                    }
                    splashVisible = false
                    // ⚠ 必须无条件卸载，不能只依赖淡出动画的 finishedListener。
                    //
                    //   开屏层里带 rememberInfiniteTransition（作者姓名呼吸动效）。
                    //   只要它还留在组合里，就会每帧驱动重组；这层又盖在整个 LiuMusicApp
                    //   之上，会持续抢走渲染管线 —— 直接后果是液态玻璃的 RenderEffect
                    //   失效，玻璃退化成一块实心背景色（也就是「液体玻璃效果没了」）。
                    //
                    //   所以这里等淡出动画（350ms）走完再硬性卸载，多给 100ms 余量。
                    delay(SPLASH_FADE_OUT_MS + 100L)
                    splashMounted = false
                }
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().alpha(pageAlpha)) {
                        LiuMusicApp(
                            openNowPlayingRequest = openNowPlayingRequest,
                            clipboardLinkRequest = clipboardLinkRequest,
                            onClipboardLinkConsumed = { clipboardLinkRequest = null },
                            playbackConnectionEnabled = playbackConnectionEnabled,
                        )
                    }
                    // ⚠ 开屏层只在「确实要显示」时挂载。
                    //   splashMounted 为 false 时这里什么都不画 —— 一个常驻的空 Box
                    //   会让上层多一层绘制节点，液态玻璃的 layerBackdrop 采样会跟着变脏。
                    if (splashMounted) {
                        LiuMusicSplashScreen(
                            visible = splashVisible,
                            onFinished = { splashMounted = false },
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            firstDraw.await()
            delay(100L)
            playbackConnectionEnabled = true
        }

        lifecycleScope.launch {
            firstDraw.await()
            delay(250L)
            LiuMusicSettingsPreferences.initialize(this@MainActivity)
        }

        // Lyricon registers a cross-process provider. Starting it before
        // setContent delayed the first frame and left a white window on cold
        // launch. Listen Together is intentionally not started here: its UI or
        // an incoming invitation creates the coordinator only when needed.
        lifecycleScope.launch {
            firstDraw.await()
            delay(500L)
            LiuMusicLyriconBridge.start(applicationContext)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumePlaybackIntent(intent)
    }

    /**
     * 实体音量键**只观察、不消费**（2026-10-07）。
     *
     * vivo 上硬件音量键不发 `VOLUME_CHANGED_ACTION`（本仓库 2026-10-06 实测），
     * 播放页只能靠 `LiuMusicPlayerUi` 里那条 150ms 轮询得知音量变了 ⇒ 按键后条最多滞后 150ms
     * 才起步，观感就是"顿一下才滑"。这里把按键信号同步推给 UI，把延迟压到 ≈1 帧。
     *
     * ⚠ 一律 `return super.onKeyDown(...)`（**绝不 return true**）：
     *   音量键的真正处理在 system_server 的 `interceptKeyBeforeQueueing`，早于事件投递给 App
     *   ⇒ 我们在这里读到的已是**调完之后**的音量；一旦消费掉事件，反而会打断系统的音量 HUD。
     *   而且这件事**必须在音量调到边界后也有信号**（越界按键在系统侧音量值不变），
     *   只能靠按键事件本身来识别 —— 这也是"过冲拉长"能做到的前提。
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //⚠ `repeatCount` 必须透传：长按时系统会自动重复发 onKeyDown（repeatCount 递增），
        //   而物理上只有**一次**手指按下。UI 侧靠它区分"新手势"与"同一手势的重复"，
        //   否则长按到极限会反复累加蓄力 ⇒ 条二次拉伸（iOS 不会，见 Bridge 注释）。
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP ->
                LiuMusicVolumeKeyBridge.onVolumeKeyDown(LiuMusicVolumeKeyBridge.DIRECTION_UP, event.repeatCount)
            KeyEvent.KEYCODE_VOLUME_DOWN ->
                LiuMusicVolumeKeyBridge.onVolumeKeyDown(LiuMusicVolumeKeyBridge.DIRECTION_DOWN, event.repeatCount)
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 松开的信号专供"过冲拉长"回弹（iOS 的橡皮筋是抬手即回，不靠超时猜）。
     * 同样只观察、不消费。
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> LiuMusicVolumeKeyBridge.onVolumeKeyUp(LiuMusicVolumeKeyBridge.DIRECTION_UP)
            KeyEvent.KEYCODE_VOLUME_DOWN -> LiuMusicVolumeKeyBridge.onVolumeKeyUp(LiuMusicVolumeKeyBridge.DIRECTION_DOWN)
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        // 传感器可用性检查后启用。无重力/方向传感器设备（极少数车机/模拟器）
        // enable 会抛 IllegalStateException，需先 try/catch 一下。不用 isDetectable()
        // 是因为 Kotlin 对匿名 object 表达式上这个 Java is 前缀方法解析不稳（unresolved）。
        orientationSensor?.let { runCatching { it.enable() } }
        val foregroundSession = LiuMusicAppVisibility.foregroundSessionId
        if (foregroundSession != handledRemoteConfigForegroundSession) {
            handledRemoteConfigForegroundSession = foregroundSession
            lifecycleScope.launch {
                firstDraw.await()
                delay(750L)
                if (LiuMusicRemoteConfigConsent.enabled(applicationContext)) {
                    LiuMusicRemoteConfigRuntime.initializeAndRefresh(
                        applicationContext,
                        BuildConfig.VERSION_CODE,
                        force = true,
                    )
                }
            }
        }
        // Shizuku is optional. On HyperOS 3, request permission only when its
        // service is actually running; permission itself acts as the user's opt-in
        // to the short XMSF compatibility pulse used by some restricted ROM builds.
        lifecycleScope.launch {
            firstDraw.await()
            delay(500L)
            HyperOsFocusBridge.prepareShizukuCompatibility(this@MainActivity)
        }

        clipboardScanJob?.cancel()
        clipboardScanJob = lifecycleScope.launch {
            firstDraw.await()
            delay(100L)
            if (!com.liuguanyi.liumusic.ui.settings.LiuMusicSettingsRuntime.clipboardLinksEnabled) return@launch
            val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = manager.primaryClip?.getItemAt(0)?.text?.toString()?.trim().orEmpty()
            if (text.isNotBlank() && text != lastClipboardText) {
                lastClipboardText = text
                val together = parseNeteaseListenTogetherInvitation(text)
                if (together != null) {
                    LiuMusicListenTogetherInviteActivity.launch(
                        this@MainActivity,
                        together.roomId,
                        together.inviterId,
                    )
                    return@launch
                }
                clipboardLinkRequest = text
            }
        }
    }

    override fun onPause() {
        // 后台不再监听旋转传感器：传感器事件本身有能耗，后台监听只耗电不提效。
        orientationSensor?.disable()
        super.onPause()
    }

    private fun completeAfterFirstDraw() {
        val decorView = window.decorView
        val listener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (decorView.viewTreeObserver.isAlive) {
                    decorView.viewTreeObserver.removeOnPreDrawListener(this)
                }
                decorView.post { firstDraw.complete(Unit) }
                return true
            }
        }
        decorView.viewTreeObserver.addOnPreDrawListener(listener)
    }

    private fun consumePlaybackIntent(intent: Intent?) {
        if (intent?.action == ACTION_OPEN_NOW_PLAYING) {
            openNowPlayingRequest += 1
        }
    }

    companion object {
        const val ACTION_OPEN_NOW_PLAYING =
            "com.liuguanyi.liumusic.action.OPEN_NOW_PLAYING"
        /** Cold starts within this window of the last backgrounding are treated as returns, not fresh launches. */
        private const val RETURN_FROM_BACKGROUND_WINDOW_MS = 15L * 60L * 1000L

    // 开屏页停留时长：够看清图标、应用名与作者信息，又不至于让老用户等得烦。
    private const val SPLASH_HOLD_MS = 1_600L

    // 开屏页淡出动画时长，卸载时机按它计算。
    private const val SPLASH_FADE_OUT_MS = 350L
    }
}
