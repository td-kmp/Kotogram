// Workaround: icons has an OffsetExt.kt in this package too, and a JVM classpath keeps only one OffsetExtKt.
@file:JvmName("ComponentsOffsetExt")

package com.xbot.kotogram.core.designsystem.extensions

import androidx.compose.ui.geometry.Offset
import kotlin.jvm.JvmName
import kotlin.math.PI
import kotlin.math.atan2

internal fun Offset.normalized(): Offset = this / getDistance()

internal infix fun Offset.dot(other: Offset): Float = x * other.x + y * other.y

internal infix fun Offset.cross(other: Offset): Float = x * other.y - y * other.x

internal fun Offset.degrees(): Float = (atan2(y, x) * HALF_TURN / PI).toFloat()

private const val HALF_TURN = 180f
