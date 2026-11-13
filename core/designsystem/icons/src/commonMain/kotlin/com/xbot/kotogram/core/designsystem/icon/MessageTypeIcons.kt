@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.roundRect
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun messageVoiceIcon() = icon("MessageVoice", canvas = 20f) {
    fill { roundRect(7f, 2f, 13f, 12.5f, 3f) }
    stroke(LINE) {
        moveTo(5f, 9.5f)
        arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 15f, 9.5f)
        line(Offset(10f, 14.5f), Offset(10f, 17.5f))
    }
}

internal fun messageAudioIcon() = icon("MessageAudio", canvas = 20f) {
    fill(PathFillType.EvenOdd) {
        circle(Offset(10f, 10f), 8f)
        moveTo(8.655f, 6.243f)
        curveTo(8.156f, 5.922f, 7.5f, 6.281f, 7.5f, 6.874f)
        lineTo(7.5f, 13.126f)
        curveTo(7.5f, 13.719f, 8.156f, 14.078f, 8.655f, 13.757f)
        lineTo(13.519f, 10.631f)
        curveTo(13.978f, 10.336f, 13.978f, 9.664f, 13.519f, 9.369f)
        close()
    }
}

internal fun messageLocationIcon() = icon("MessageLocation", canvas = 20f) {
    fill(PathFillType.EvenOdd) {
        moveTo(10f, 19.111f)
        curveTo(12.015f, 19.111f, 16.667f, 12.793f, 16.667f, 8f)
        arcTo(6.667f, 6.667f, 0f, isMoreThanHalf = false, isPositiveArc = false, 3.333f, 8f)
        curveTo(3.333f, 12.793f, 7.985f, 19.111f, 10f, 19.111f)
        close()
        circle(Offset(10f, 8f), 2.667f)
    }
}

internal fun messagePollIcon() = icon("MessagePoll", canvas = 20f) {
    stroke(3.2f) {
        line(Offset(4.8f, 8.8f), Offset(4.8f, 15.2f))
        line(Offset(10f, 4.8f), Offset(10f, 15.2f))
        line(Offset(15.2f, 11.2f), Offset(15.2f, 15.2f))
    }
}

internal fun messageCallIncomingIcon() = callIcon(
    name = "MessageCallIncoming",
    corner = Offset(11.33f, 8.67f),
    ends = [Offset(11.33f, 4.34f), Offset(15.66f, 8.67f), Offset(16.33f, 3.67f)],
)

internal fun messageCallOutgoingIcon() = callIcon(
    name = "MessageCallOutgoing",
    corner = Offset(16.33f, 3.67f),
    ends = [Offset(12f, 3.67f), Offset(16.33f, 8f), Offset(11.33f, 8.67f)],
)

internal fun thumbnailPlayIcon() = icon("ThumbnailPlay", canvas = 16f) {
    fill {
        moveTo(6.777f, 4.518f)
        lineTo(11.376f, 7.584f)
        curveTo(11.606f, 7.737f, 11.668f, 8.048f, 11.515f, 8.277f)
        curveTo(11.478f, 8.332f, 11.431f, 8.379f, 11.376f, 8.416f)
        lineTo(6.777f, 11.482f)
        curveTo(6.548f, 11.635f, 6.237f, 11.573f, 6.084f, 11.343f)
        curveTo(6.029f, 11.261f, 6f, 11.164f, 6f, 11.066f)
        lineTo(6f, 4.934f)
        curveTo(6f, 4.658f, 6.224f, 4.434f, 6.5f, 4.434f)
        curveTo(6.599f, 4.434f, 6.695f, 4.463f, 6.777f, 4.518f)
        close()
    }
}

private fun callIcon(
    name: String,
    corner: Offset,
    ends: List<Offset>,
): ImageVector = icon(name, canvas = 20f) {
    fill {
        moveTo(5.502f, 3f)
        curveTo(5.915f, 3f, 6.29f, 3.169f, 6.558f, 3.544f)
        lineTo(7.856f, 5.644f)
        curveTo(8.249f, 6.279f, 8.211f, 7.09f, 7.761f, 7.686f)
        lineTo(6.842f, 8.899f)
        curveTo(6.758f, 8.984f, 6.728f, 9.076f, 6.728f, 9.176f)
        curveTo(6.728f, 9.268f, 6.766f, 9.367f, 6.804f, 9.451f)
        curveTo(7.026f, 9.918f, 7.691f, 10.729f, 8.472f, 11.51f)
        curveTo(9.244f, 12.291f, 10.071f, 12.941f, 10.522f, 13.179f)
        curveTo(10.606f, 13.217f, 10.706f, 13.255f, 10.812f, 13.255f)
        curveTo(10.92f, 13.255f, 11.019f, 13.225f, 11.104f, 13.141f)
        lineTo(12.29f, 12.229f)
        curveTo(12.887f, 11.77f, 13.705f, 11.729f, 14.345f, 12.126f)
        lineTo(16.487f, 13.456f)
        curveTo(16.862f, 13.716f, 17f, 14.068f, 17f, 14.436f)
        curveTo(17f, 14.948f, 16.709f, 15.484f, 16.319f, 15.897f)
        curveTo(15.616f, 16.655f, 14.758f, 17f, 13.756f, 17f)
        curveTo(11.568f, 17f, 8.914f, 15.323f, 6.787f, 13.195f)
        curveTo(4.653f, 11.06f, 3f, 8.427f, 3f, 6.238f)
        curveTo(3f, 5.266f, 3.321f, 4.386f, 4.056f, 3.674f)
        curveTo(4.499f, 3.238f, 5.02f, 3f, 5.502f, 3f)
        close()
    }
    // Workaround: CMP 1.12's Skia paint never passes StrokeJoin.Round on; lone segments' round caps join round.
    stroke(LINE) { ends.forEach { end -> line(corner, end) } }
}
