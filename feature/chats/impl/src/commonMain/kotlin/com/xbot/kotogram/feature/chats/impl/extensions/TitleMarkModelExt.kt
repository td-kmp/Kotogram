package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.component.TitleMark
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.mark_fake
import com.xbot.kotogram.core.resources.mark_scam
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.screen.list.TitleMarkModel

@Composable
internal fun TitleMarkModel.toMark(): TitleMark = when (this) {
    Verified -> TitleMark.Verified
    Premium -> TitleMark.Premium
    Scam -> TitleMark.Warning(stringResource(Res.string.mark_scam))
    Fake -> TitleMark.Warning(stringResource(Res.string.mark_fake))
}
