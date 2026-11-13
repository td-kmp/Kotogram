@file:Suppress("MagicNumber")

package com.xbot.kotogram.core.designsystem.icon

import androidx.compose.ui.geometry.Offset
import com.xbot.kotogram.core.designsystem.extensions.circle
import com.xbot.kotogram.core.designsystem.extensions.fill
import com.xbot.kotogram.core.designsystem.extensions.line
import com.xbot.kotogram.core.designsystem.extensions.polygon
import com.xbot.kotogram.core.designsystem.extensions.stroke

internal fun pinFilledIcon() = icon("PinFilled", canvas = 20f) {
    fill {
        polygon(
            [
                Offset(12.5f, 2.744f),
                Offset(11.5f, 3.645f),
                Offset(9.5f, 6.415f),
                Offset(7.167f, 6.375f),
                Offset(5.833f, 6.774f),
                Offset(4.673f, 7.5f),
                Offset(4.499f, 7.833f),
                Offset(4.783f, 8.5f),
                Offset(6.783f, 10.5f),
                Offset(6.956f, 10.833f),
                Offset(3.374f, 15.833f),
                Offset(2.711f, 16.833f),
                Offset(2.833f, 16.956f),
                Offset(8.833f, 12.711f),
                Offset(11.5f, 15.096f),
                Offset(11.833f, 15.168f),
                Offset(12.316f, 14.833f),
                Offset(12.898f, 13.833f),
                Offset(13.292f, 12.5f),
                Offset(13.252f, 10.167f),
                Offset(16.418f, 7.833f),
                Offset(16.922f, 7.167f),
                Offset(16.988f, 6.833f),
                Offset(16.872f, 6.167f),
                Offset(16.653f, 5.833f),
                Offset(14.016f, 3.167f),
                Offset(13.167f, 2.7f),
            ],
        )
    }
}

internal fun forwardFilledIcon() = icon("ForwardFilled", canvas = 14f) {
    fill {
        moveTo(7.214f, 4.48f)
        lineTo(7.214f, 3.268f)
        curveTo(7.214f, 2.457f, 7.214f, 2.051f, 7.377f, 1.856f)
        curveTo(7.518f, 1.687f, 7.731f, 1.594f, 7.951f, 1.606f)
        curveTo(8.204f, 1.619f, 8.502f, 1.895f, 9.096f, 2.447f)
        lineTo(13.116f, 6.179f)
        curveTo(13.423f, 6.464f, 13.577f, 6.607f, 13.634f, 6.774f)
        curveTo(13.684f, 6.92f, 13.684f, 7.08f, 13.634f, 7.226f)
        curveTo(13.577f, 7.393f, 13.423f, 7.536f, 13.116f, 7.821f)
        lineTo(9.096f, 11.553f)
        curveTo(8.502f, 12.105f, 8.204f, 12.381f, 7.951f, 12.394f)
        curveTo(7.731f, 12.406f, 7.518f, 12.313f, 7.377f, 12.144f)
        curveTo(7.214f, 11.949f, 7.214f, 11.543f, 7.214f, 10.732f)
        lineTo(7.214f, 9.52f)
        curveTo(4.072f, 9.52f, 2.169f, 10.748f, 1.114f, 11.804f)
        curveTo(0.644f, 12.275f, 0.409f, 12.51f, 0.3f, 12.514f)
        curveTo(0.197f, 12.518f, 0.119f, 12.479f, 0.06f, 12.395f)
        curveTo(-0.003f, 12.306f, 0.035f, 12.016f, 0.113f, 11.436f)
        curveTo(0.46f, 8.848f, 1.842f, 4.48f, 7.214f, 4.48f)
        close()
    }
}

internal fun mentionBadgeIcon() = icon("MentionBadge", canvas = 20f) {
    stroke(2f) {
        circle(Offset(10f, 10f), 3.857f)
        moveTo(13.857f, 6.143f)
        lineTo(13.857f, 11.286f)
        arcTo(2.571f, 2.571f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19f, 11.286f)
        lineTo(19f, 10f)
        arcTo(9f, 9f, 0f, isMoreThanHalf = true, isPositiveArc = false, 13.335f, 18.359f)
    }
}

internal fun reactionBadgeIcon() = icon("ReactionBadge", canvas = 20f) {
    fill {
        moveTo(10f, 18.636f)
        curveTo(10.245f, 18.636f, 10.593f, 18.457f, 10.875f, 18.278f)
        curveTo(16.133f, 14.892f, 19.5f, 10.923f, 19.5f, 6.897f)
        curveTo(19.5f, 3.445f, 17.12f, 1.037f, 14.12f, 1.037f)
        curveTo(12.248f, 1.037f, 10.847f, 2.072f, 10f, 3.624f)
        curveTo(9.172f, 2.081f, 7.761f, 1.037f, 5.89f, 1.037f)
        curveTo(2.889f, 1.037f, 0.5f, 3.445f, 0.5f, 6.897f)
        curveTo(0.5f, 10.923f, 3.867f, 14.892f, 9.125f, 18.278f)
        curveTo(9.417f, 18.457f, 9.765f, 18.636f, 10f, 18.636f)
        close()
    }
}

internal fun verifiedBadgeIcon() = icon("VerifiedBadge", canvas = 16f) {
    fill {
        moveTo(0.566f, 8.348f)
        curveTo(0.649f, 8.604f, 0.872f, 8.827f, 1.318f, 9.273f)
        lineTo(2.375f, 10.33f)
        lineTo(2.375f, 11.825f)
        curveTo(2.375f, 12.455f, 2.375f, 12.77f, 2.498f, 13.011f)
        curveTo(2.605f, 13.222f, 2.778f, 13.394f, 2.989f, 13.502f)
        curveTo(3.23f, 13.625f, 3.545f, 13.625f, 4.175f, 13.625f)
        lineTo(5.67f, 13.625f)
        lineTo(6.727f, 14.682f)
        curveTo(7.172f, 15.128f, 7.395f, 15.35f, 7.652f, 15.434f)
        curveTo(7.878f, 15.507f, 8.121f, 15.507f, 8.347f, 15.434f)
        curveTo(8.604f, 15.35f, 8.827f, 15.128f, 9.272f, 14.682f)
        lineTo(10.33f, 13.625f)
        lineTo(11.825f, 13.625f)
        curveTo(12.455f, 13.625f, 12.77f, 13.625f, 13.011f, 13.502f)
        curveTo(13.222f, 13.394f, 13.394f, 13.222f, 13.502f, 13.011f)
        curveTo(13.625f, 12.77f, 13.625f, 12.455f, 13.625f, 11.825f)
        lineTo(13.625f, 10.33f)
        lineTo(14.682f, 9.273f)
        curveTo(15.127f, 8.827f, 15.35f, 8.604f, 15.434f, 8.348f)
        curveTo(15.507f, 8.122f, 15.507f, 7.878f, 15.434f, 7.652f)
        curveTo(15.35f, 7.395f, 15.127f, 7.173f, 14.682f, 6.727f)
        lineTo(13.625f, 5.67f)
        lineTo(13.625f, 4.175f)
        curveTo(13.625f, 3.545f, 13.625f, 3.23f, 13.502f, 2.989f)
        curveTo(13.394f, 2.778f, 13.222f, 2.605f, 13.011f, 2.498f)
        curveTo(12.77f, 2.375f, 12.455f, 2.375f, 11.825f, 2.375f)
        lineTo(10.33f, 2.375f)
        lineTo(9.272f, 1.318f)
        curveTo(8.827f, 0.872f, 8.604f, 0.65f, 8.347f, 0.566f)
        curveTo(8.121f, 0.493f, 7.878f, 0.493f, 7.652f, 0.566f)
        curveTo(7.395f, 0.65f, 7.172f, 0.872f, 6.727f, 1.318f)
        lineTo(5.67f, 2.375f)
        lineTo(4.175f, 2.375f)
        curveTo(3.545f, 2.375f, 3.23f, 2.375f, 2.989f, 2.498f)
        curveTo(2.778f, 2.605f, 2.605f, 2.778f, 2.498f, 2.989f)
        curveTo(2.375f, 3.23f, 2.375f, 3.545f, 2.375f, 4.175f)
        lineTo(2.375f, 5.67f)
        lineTo(1.318f, 6.727f)
        curveTo(0.872f, 7.173f, 0.649f, 7.395f, 0.566f, 7.652f)
        curveTo(0.492f, 7.878f, 0.492f, 8.122f, 0.566f, 8.348f)
        close()
    }
}

internal fun verifiedCheckIcon() = icon("VerifiedCheck", canvas = 16f) {
    // Workaround: CMP 1.12's Skia paint never passes StrokeJoin.Round on; lone segments' round caps join round.
    stroke(1.5f) {
        line(Offset(5.375f, 8.29f), Offset(7.125f, 10.04f))
        line(Offset(7.125f, 10.04f), Offset(10.625f, 6.54f))
    }
}
