package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.KotogramTheme

internal val TypographyPage = CatalogPage(
    id = "typography",
    section = CatalogSection.Foundations,
    name = "Typography",
    description = "Two scales in Material 3's roles, switched by the theme: Telegram for iOS's sizes on touch " +
        "screens, Telegram for macOS's on laptops (toggle Mobile). Each role carries SF Pro's tracking for its " +
        "size. Apple platforms draw them in SF Pro; Android, the web, Windows and Linux in Inter.",
    examples = [
        CatalogExample("Roles") {
            val typography = KotogramTheme.typography
            TypeScale(
                "headlineLarge" to typography.headlineLarge,
                "headlineLargeEmphasized" to typography.headlineLargeEmphasized,
                "headlineMedium" to typography.headlineMedium,
                "headlineSmall" to typography.headlineSmall,
                "titleLarge" to typography.titleLarge,
                "titleMedium" to typography.titleMedium,
                "titleSmall" to typography.titleSmall,
                "bodyLarge" to typography.bodyLarge,
                "bodyLargeEmphasized" to typography.bodyLargeEmphasized,
                "bodyMedium" to typography.bodyMedium,
                "bodySmall" to typography.bodySmall,
                "labelLarge" to typography.labelLarge,
                "labelMedium" to typography.labelMedium,
                "labelSmall" to typography.labelSmall,
                "labelSmallEmphasized" to typography.labelSmallEmphasized,
            )
        },
        CatalogExample("Weights") {
            Column(verticalArrangement = Arrangement.spacedBy(KotogramTheme.spacing.small)) {
                [FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold].forEach { weight ->
                    Text(
                        text = "${weightName(weight)} ${weight.weight} · $SAMPLE",
                        style = KotogramTheme.typography.bodyLarge,
                        fontWeight = weight,
                    )
                }
            }
        },
        CatalogExample("Scripts") {
            Text(
                text = "Telegram · Телеграм · 0123456789 · «Hello» — ¿Qué tal?",
                style = KotogramTheme.typography.bodyLarge,
            )
        },
        CatalogExample("Material 3's display roles") {
            val typography = KotogramTheme.typography
            TypeScale(
                "displayLarge" to typography.displayLarge,
                "displayMedium" to typography.displayMedium,
                "displaySmall" to typography.displaySmall,
                sample = "Telegram",
            )
        },
    ],
)

@Composable
private fun TypeScale(
    vararg roles: Pair<String, TextStyle>,
    sample: String = SAMPLE,
) {
    val spacing = KotogramTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        roles.forEach { (name, style) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$name · ${size(style.fontSize.value)} ${weightName(style.fontWeight)}",
                    style = KotogramTheme.typography.bodySmall,
                    color = KotogramTheme.colors.onSurfaceVariant,
                    modifier = Modifier.width(LabelWidth),
                )
                Text(sample, style = style)
            }
        }
    }
}

private fun size(value: Float): String = if (value % 1f == 0f) value.toInt().toString() else value.toString()

private fun weightName(weight: FontWeight?): String = when (weight) {
    FontWeight.Medium -> "medium"
    FontWeight.SemiBold -> "semibold"
    FontWeight.Bold -> "bold"
    else -> "regular"
}

private const val SAMPLE = "Sign in to Telegram"
private val LabelWidth = 250.dp
