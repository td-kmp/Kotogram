// Hack: Compose resources keep their CLDR rules and environment internal; every use stays in this file.
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(InternalResourceApi::class)

package com.xbot.kotogram.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import io.xbot.tdlib.api.LanguagePackStringValuePluralized
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.DefaultComposeEnvironment
import org.jetbrains.compose.resources.DensityQualifier
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.ScriptQualifier
import org.jetbrains.compose.resources.ThemeQualifier
import org.jetbrains.compose.resources.plural.PluralCategory
import org.jetbrains.compose.resources.plural.PluralRule
import org.jetbrains.compose.resources.plural.PluralRuleList
import org.jetbrains.compose.resources.plural.cldrPluralRuleListIndexByLocale
import org.jetbrains.compose.resources.plural.cldrPluralRuleLists

private const val BUNDLED_LANGUAGE = "en"

private val ruleLists = Array(cldrPluralRuleLists.size) { index ->
    lazy {
        val rules = cldrPluralRuleLists[index].map { (category, rule) -> PluralRule(category, rule) }
        PluralRuleList(rules.toTypedArray())
    }
}

private val categories = [
    PluralCategory.ZERO,
    PluralCategory.ONE,
    PluralCategory.TWO,
    PluralCategory.FEW,
    PluralCategory.MANY,
    PluralCategory.OTHER,
]

internal fun pluralForm(
    value: LanguagePackStringValuePluralized,
    pluralCode: String,
    quantity: Int,
): String {
    val forms = with(value) { [zeroValue, oneValue, twoValue, fewValue, manyValue, otherValue] }
    return forms[formIndex(pluralCode, quantity)].ifEmpty { value.otherValue }
}

private fun formIndex(
    pluralCode: String,
    quantity: Int,
): Int {
    val parts = pluralCode.split('-', '_')
    val language = parts.first().lowercase()
    val region = parts.getOrNull(1)?.uppercase()
    val index = region?.let { cldrPluralRuleListIndexByLocale["${language}_$it"] }
        ?: cldrPluralRuleListIndexByLocale[language]
        ?: return categories.lastIndex
    return categories.indexOf(ruleLists[index].value.getCategory(quantity))
}

internal fun bundledComposeEnvironment(): ProvidedValue<*> = LocalComposeEnvironment provides BundledComposeEnvironment

internal val bundledStringEnvironment: ResourceEnvironment =
    bundledEnvironment(ThemeQualifier.LIGHT, DensityQualifier.MDPI)

private object BundledComposeEnvironment : ComposeEnvironment {
    @Composable
    override fun rememberEnvironment(): ResourceEnvironment {
        val device = DefaultComposeEnvironment.rememberEnvironment()
        return remember(device) { bundledEnvironment(device.theme, device.density) }
    }
}

private fun bundledEnvironment(
    theme: ThemeQualifier,
    density: DensityQualifier,
) = ResourceEnvironment(
    language = LanguageQualifier(BUNDLED_LANGUAGE),
    script = ScriptQualifier(""),
    region = RegionQualifier(""),
    theme = theme,
    density = density,
)
