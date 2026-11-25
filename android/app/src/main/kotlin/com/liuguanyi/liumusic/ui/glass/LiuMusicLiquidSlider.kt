package com.liuguanyi.liumusic.ui.glass

/*
 * Thin LiuMusic wrapper around the directly ported AndroidLiquidGlass Catalog demo.
 * Upstream is licensed under Apache-2.0:
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/main/app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/LiquidSlider.kt
 */

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import com.liuguanyi.liumusic.ui.glass.liquidsliderdemo.LiuMusicDemoLiquidSlider
import com.liuguanyi.liumusic.ui.settings.LiuMusicSettingsRuntime
import kotlin.math.roundToInt

@Composable
fun LiuMusicLiquidSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onTransientValueChange: (Float) -> Unit = {},
    onValueChangeFinished: () -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float>,
    stepSize: Float = 0f,
    visibilityThreshold: Float = 0.01f,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    require(valueRange.endInclusive > valueRange.start) { "valueRange must not be empty" }
    require(stepSize >= 0f) { "stepSize must be non-negative" }
    val externalValue = quantizeLiuMusicSliderValue(value, valueRange, stepSize)
    var transientValue by remember { mutableFloatStateOf(externalValue) }
    var interacting by remember { mutableStateOf(false) }
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val latestOnTransientValueChange by rememberUpdatedState(onTransientValueChange)
    val latestOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
    val steps = if (stepSize > 0f) {
        (((valueRange.endInclusive - valueRange.start) / stepSize).roundToInt() - 1).coerceAtLeast(0)
    } else 0

    LaunchedEffect(externalValue, interacting) {
        if (!interacting) transientValue = externalValue
    }

    fun finish() {
        val settled = quantizeLiuMusicSliderValue(transientValue, valueRange, stepSize)
        transientValue = settled
        interacting = false
        if (settled != externalValue) latestOnValueChange(settled)
        latestOnValueChangeFinished()
    }

    val semantics = Modifier.semantics(mergeDescendants = true) {
        if (contentDescription != null) this.contentDescription = contentDescription
        progressBarRangeInfo = ProgressBarRangeInfo(transientValue, valueRange, steps)
        if (enabled) {
            setProgress { requested ->
                transientValue = quantizeLiuMusicSliderValue(requested, valueRange, stepSize)
                finish()
                true
            }
        }
    }
    // ⚠ 走门控入口，不要直接读 `LocalLiuMusicBackdrop.current`（见 `LiuMusicGlassSafety.kt` 说明）。
    val backdrop = liuMusicGlassBackdrop()
    if (backdrop == null || LiuMusicSettingsRuntime.frostedGlassEnabled) {
        Slider(
            value = transientValue,
            onValueChange = {
                interacting = true
                transientValue = it.coerceIn(valueRange)
                latestOnTransientValueChange(transientValue)
            },
            onValueChangeFinished = ::finish,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            modifier = modifier.then(semantics),
        )
        return
    }

    LiuMusicDemoLiquidSlider(
        value = { transientValue },
        onValueChange = {
            interacting = true
            transientValue = it.coerceIn(valueRange)
            latestOnTransientValueChange(transientValue)
        },
        onValueChangeFinished = ::finish,
        valueRange = valueRange,
        visibilityThreshold = visibilityThreshold,
        backdrop = backdrop,
        enabled = enabled,
        modifier = modifier.then(semantics),
    )
}

internal fun quantizeLiuMusicSliderValue(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    stepSize: Float,
): Float {
    val clamped = value.coerceIn(range.start, range.endInclusive)
    if (stepSize <= 0f) return clamped
    val steps = ((clamped - range.start) / stepSize).roundToInt()
    return (range.start + steps * stepSize).coerceIn(range.start, range.endInclusive)
}
