package com.xbot.kotogram.core.designsystem.extensions

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset

@RequiresApi(Build.VERSION_CODES.Q)
internal fun View.surfaceOffset(): Offset {
    val inSurface = IntArray(2)
    val inWindow = IntArray(2)
    getLocationInSurface(inSurface)
    getLocationInWindow(inWindow)
    return Offset((inSurface[0] - inWindow[0]).toFloat(), (inSurface[1] - inWindow[1]).toFloat())
}
