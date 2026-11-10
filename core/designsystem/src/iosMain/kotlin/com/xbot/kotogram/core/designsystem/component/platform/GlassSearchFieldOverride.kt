package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitView
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.SearchFieldOverride
import com.xbot.kotogram.core.designsystem.component.SearchFieldOverrideScope
import com.xbot.kotogram.core.designsystem.extensions.appleWeight
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import platform.UIKit.UIFont

internal object GlassSearchFieldOverride : SearchFieldOverride {
    @Composable
    override fun SearchFieldOverrideScope.SearchField() {
        val currentState by rememberUpdatedState(state)
        val searchView = remember { GlassSearchView { currentState.setTextAndPlaceCursorAtEnd(it) } }
        val text = state.text.toString()
        val textStyle = style.textStyle
        val fontSize = with(LocalDensity.current) { textStyle.fontSize.toDp().value.toDouble() }
        val font = UIFont.systemFontOfSize(fontSize, textStyle.fontWeight.appleWeight())
        val textColor = textStyle.color.toUIColor()
        val placeholderColor = style.placeholderColor.toUIColor()
        val tintColor = style.cursorColor.toUIColor()
        val dark = KotogramTheme.colors.background.isDark()
        val inFront = isSettledInFront()
        UIKitView(
            factory = { searchView.root.apply { alpha = 0.0 } },
            modifier = modifier.height(style.height),
            update = { view ->
                searchView.update(text, placeholder, font, textColor, placeholderColor, tintColor)
                view.overrideUserInterfaceStyle = interfaceStyle(dark)
                view.userInteractionEnabled = inFront
                fade(view, visible = inFront)
            },
            properties = GlassProperties,
        )
    }
}
