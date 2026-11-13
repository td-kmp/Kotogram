package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldOrientation
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldState
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowPosture

internal val WindowPosture.isBook: Boolean
    get() = folds.any { it.state == HalfOpened && it.orientation == Vertical }
