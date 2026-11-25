package com.liuguanyi.liumusic.ui.discovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.liuguanyi.liumusic.R
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * 首页问候语的分时段档位。
 *
 * 时间段划分（本地时区，按小时）：
 * - 05:00–10:59 早上好
 * - 11:00–12:59 中午好
 * - 13:00–17:59 下午好
 * - 18:00–22:59 晚上好
 * - 23:00–04:59 夜深了
 *
 * 这是最常见的分法：早上从 5 点起算（凌晨 4 点不该说「早上好」），
 * 深夜单独一档，避免凌晨三点还在说「晚上好」。
 */
private enum class GreetingSlot { Morning, Noon, Afternoon, Evening, Night }

private fun currentSlot(hour: Int): GreetingSlot = when (hour) {
    in 5..10 -> GreetingSlot.Morning
    in 11..12 -> GreetingSlot.Noon
    in 13..17 -> GreetingSlot.Afternoon
    in 18..22 -> GreetingSlot.Evening
    else -> GreetingSlot.Night // 23, 0, 1, 2, 3, 4
}

@Composable
private fun GreetingSlot.text(): String = when (this) {
    GreetingSlot.Morning -> stringResource(R.string.home_greeting_morning)
    GreetingSlot.Noon -> stringResource(R.string.home_greeting_noon)
    GreetingSlot.Afternoon -> stringResource(R.string.home_greeting_afternoon)
    GreetingSlot.Evening -> stringResource(R.string.home_greeting_evening)
    GreetingSlot.Night -> stringResource(R.string.home_greeting_night)
}

/**
 * 按真实本地时间给出的问候语。
 *
 * 每 60 秒重新读一次时钟：用户把 App 挂在后台跨过整点再回来，
 * 问候语也会跟着变，而不是停在打开那一刻的档位。
 *
 * 只读系统时间，不做网络校时 —— 设备时区由用户自己设定，跟随它才符合直觉。
 */
@Composable
internal fun rememberTimeAwareGreeting(): String {
    var hour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            val now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (now != hour) hour = now
        }
    }
    return currentSlot(hour).text()
}
