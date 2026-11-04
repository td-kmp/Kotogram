package com.xbot.kotogram.feature.auth.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.feature.auth.impl.component.UiText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UiText.asString(): String = stringResource(resource, *args)
