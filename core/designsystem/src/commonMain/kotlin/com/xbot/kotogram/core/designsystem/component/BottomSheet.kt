package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle
import kotlinx.coroutines.launch

// Shown while composed: UIKit's page sheet on iOS, Material 3's modal bottom sheet elsewhere. Its content brings
// its own top bar, and closes the sheet with BottomSheetScope.dismiss.
@Composable
fun BottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    style: BottomSheetStyle = BottomSheetStyle,
    content: @Composable BottomSheetScope.() -> Unit
) {
    val sheetStyle = rememberStyle(::BottomSheetStyleScope, BottomSheetStyle.Default then style)
    with(LocalBottomSheetOverride.current) {
        BottomSheetOverrideScope(onDismissRequest, modifier, sheetStyle, content).BottomSheet()
    }
}

interface BottomSheetScope : ColumnScope {
    // Hides the sheet the way a swipe does, then calls onDismissRequest.
    fun dismiss()
}

internal class DefaultBottomSheetScope(column: ColumnScope, private val onDismiss: () -> Unit) :
    BottomSheetScope,
    ColumnScope by column {
    override fun dismiss() = onDismiss()
}

internal interface BottomSheetOverride {
    @Composable
    fun BottomSheetOverrideScope.BottomSheet()
}

internal class BottomSheetOverrideScope(
    val onDismissRequest: () -> Unit,
    val modifier: Modifier,
    val style: BottomSheetStyleScope,
    val content: @Composable BottomSheetScope.() -> Unit
)

internal object DefaultBottomSheetOverride : BottomSheetOverride {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BottomSheetOverrideScope.BottomSheet() {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
        // Material 3's own way to close it in code: hide, then leave the composition.
        val dismiss: () -> Unit = remember(sheetState, scope) {
            {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) currentOnDismissRequest()
                }
            }
        }
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetState = sheetState,
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            dragHandle = null
        ) {
            DefaultBottomSheetScope(this, dismiss).content()
        }
    }
}

internal val LocalBottomSheetOverride: ProvidableCompositionLocal<BottomSheetOverride> =
    staticCompositionLocalOf { DefaultBottomSheetOverride }

// The screen's background, as Telegram's sheets have. No drag handle: the content's top bar closes it.
fun interface BottomSheetStyle : ComponentStyle<BottomSheetStyleScope> {
    companion object : BottomSheetStyle {
        override fun BottomSheetStyleScope.applyStyle() {}

        val Default = BottomSheetStyle {
            containerColor(theme.colors.background)
            contentColor(theme.colors.onBackground)
        }
    }
}

infix fun BottomSheetStyle.then(other: BottomSheetStyle): BottomSheetStyle = when {
    other === BottomSheetStyle -> this
    this === BottomSheetStyle -> other
    else -> BottomSheetStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class BottomSheetStyleScope(val theme: KotogramTheme.Values, override val mediaQueryInfo: MediaQueryInfo) :
    AdaptiveStyleScope<BottomSheetStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var containerColor: Color = Color.Unspecified
        private set

    var contentColor: Color = Color.Unspecified
        private set

    fun containerColor(color: Color) {
        containerColor = color
    }

    fun contentColor(color: Color) {
        contentColor = color
    }
}
