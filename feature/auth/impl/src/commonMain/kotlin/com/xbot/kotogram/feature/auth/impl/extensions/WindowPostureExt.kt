package com.xbot.kotogram.feature.auth.impl.extensions

import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldOrientation
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.FoldState
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowPosture

// The book posture of Android's foldable guidance, which WindowPosture dropped (aosp/4249142).
internal val WindowPosture.isBook: Boolean
    get() = folds.any { it.state == FoldState.HalfOpened && it.orientation == FoldOrientation.Vertical }
