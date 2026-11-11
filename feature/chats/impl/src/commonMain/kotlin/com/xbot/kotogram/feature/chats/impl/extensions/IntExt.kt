package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.messagetimer_shortdays
import com.xbot.kotogram.core.resources.messagetimer_shorthours
import com.xbot.kotogram.core.resources.messagetimer_shortminutes
import com.xbot.kotogram.core.resources.messagetimer_shortmonths
import com.xbot.kotogram.core.resources.messagetimer_shortseconds
import com.xbot.kotogram.core.resources.messagetimer_shortweeks
import com.xbot.kotogram.core.resources.stringResource

@Composable
internal fun Int.shortTimerString(): String {
    val seconds = this
    val (resource, value) = when {
        seconds >= MONTH -> Res.string.messagetimer_shortmonths to seconds / MONTH
        seconds >= WEEK -> Res.string.messagetimer_shortweeks to seconds / WEEK
        seconds >= DAY -> Res.string.messagetimer_shortdays to seconds / DAY
        seconds >= HOUR -> Res.string.messagetimer_shorthours to seconds / HOUR
        seconds >= MINUTE -> Res.string.messagetimer_shortminutes to seconds / MINUTE
        else -> Res.string.messagetimer_shortseconds to seconds
    }
    return stringResource(resource, value.toString())
}

private const val MINUTE = 60
private const val HOUR = 3_600
private const val DAY = 86_400
private const val WEEK = 604_800
private const val MONTH = 2_592_000
