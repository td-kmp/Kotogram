package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xbot.kotogram.core.designsystem.KotogramLaptopPreview
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons
import com.xbot.kotogram.core.designsystem.style.AdaptiveStyleScope
import com.xbot.kotogram.core.designsystem.style.ComponentStyle
import com.xbot.kotogram.core.designsystem.style.DefaultStyleResolver
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.StyleResolver
import com.xbot.kotogram.core.designsystem.style.rememberStyle

@Composable
fun SearchField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    style: SearchFieldStyle = SearchFieldStyle,
) {
    val fieldStyle = rememberStyle(::SearchFieldStyleScope, SearchFieldStyle.Adaptive then style)
    with(LocalSearchFieldOverride.current) {
        SearchFieldOverrideScope(state, placeholder, modifier, fieldStyle).SearchField()
    }
}

internal interface SearchFieldOverride {
    @Composable
    fun SearchFieldOverrideScope.SearchField()
}

internal class SearchFieldOverrideScope(
    val state: TextFieldState,
    val placeholder: String,
    val modifier: Modifier,
    val style: SearchFieldStyleScope,
)

internal object DefaultSearchFieldOverride : SearchFieldOverride {
    @Composable
    override fun SearchFieldOverrideScope.SearchField() {
        val textFieldStyle = remember(style) { style.textFieldStyle() }
        TextField(
            state = state,
            modifier = modifier,
            placeholder = { Text(placeholder, maxLines = 1) },
            leadingIcon = { Icon(KotogramIcons.Search, contentDescription = null, tint = style.iconColor) },
            trailingIcon = if (state.text.isEmpty()) {
                null
            } else {
                {
                    Icon(
                        imageVector = KotogramIcons.Clear,
                        contentDescription = null,
                        modifier = Modifier.clickable(role = Role.Button) { state.clearText() },
                        tint = style.iconColor,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            lineLimits = TextFieldLineLimits.SingleLine,
            style = textFieldStyle,
        )
    }
}

internal val LocalSearchFieldOverride: ProvidableCompositionLocal<SearchFieldOverride> =
    staticCompositionLocalOf { DefaultSearchFieldOverride }

private fun SearchFieldStyleScope.textFieldStyle(): TextFieldStyle {
    val search = this
    return TextFieldStyle {
        minWidth(0.dp)
        minHeight(search.height)
        shape(search.shape)
        contentPadding(search.contentPadding)
        textStyle(search.textStyle)
        placeholderTextStyle(search.textStyle.copy(color = search.placeholderColor))
        iconSize(search.iconSize)
        iconInset(search.iconInset)
        focusedIndicatorColor(Color.Transparent)
        unfocusedIndicatorColor(Color.Transparent)
    }
}

@KotogramPreview
@Composable
private fun SearchFieldPreview() {
    SearchField(
        state = rememberTextFieldState(),
        modifier = Modifier.fillMaxWidth().padding(KotogramTheme.spacing.large),
        placeholder = "Search",
    )
}

@KotogramLaptopPreview
@Composable
private fun SearchFieldLaptopPreview() = SearchFieldPreview()

@Suppress("MagicNumber")
fun interface SearchFieldStyle : ComponentStyle<SearchFieldStyleScope> {
    companion object : SearchFieldStyle {
        override fun SearchFieldStyleScope.applyStyle() {}

        val Default = SearchFieldStyle {
            height(48.dp)
            shape(CircleShape)
            contentPadding(PaddingValues(horizontal = 16.dp))
            textStyle(theme.typography.bodyLarge.merge(fontSize = 17.sp, color = theme.colors.onSurface))
            placeholderColor(theme.colors.onSurfaceVariant)
            iconColor(theme.colors.onSurfaceVariant)
            cursorColor(theme.colors.primary)
            iconSize(32.dp)
            iconInset(8.dp)
        }

        val Adaptive = Default then SearchFieldStyle {
            laptop {
                height(32.dp)
                contentPadding(PaddingValues(horizontal = 10.dp))
                textStyle(theme.typography.bodyLarge.merge(color = theme.colors.onSurface))
                iconSize(24.dp)
                iconInset(4.dp)
            }
        }
    }
}

infix fun SearchFieldStyle.then(other: SearchFieldStyle): SearchFieldStyle = when {
    other === SearchFieldStyle -> this

    this === SearchFieldStyle -> other

    else -> SearchFieldStyle {
        with(this@then) { applyStyle() }
        with(other) { applyStyle() }
    }
}

class SearchFieldStyleScope(
    val theme: KotogramTheme.Values,
    override val mediaQueryInfo: MediaQueryInfo,
) : AdaptiveStyleScope<SearchFieldStyleScope>,
    StyleResolver by DefaultStyleResolver() {

    var height: Dp = Dp.Unspecified
        private set

    var shape: Shape = RectangleShape
        private set

    var contentPadding: PaddingValues = PaddingValues()
        private set

    var textStyle: TextStyle = TextStyle.Default
        private set

    var placeholderColor: Color = Color.Unspecified
        private set

    var iconColor: Color = Color.Unspecified
        private set

    var cursorColor: Color = Color.Unspecified
        private set

    var iconSize: Dp = Dp.Unspecified
        private set

    var iconInset: Dp = Dp.Unspecified
        private set

    fun height(height: Dp) {
        this.height = height
    }

    fun shape(shape: Shape) {
        this.shape = shape
    }

    fun contentPadding(padding: PaddingValues) {
        contentPadding = padding
    }

    fun textStyle(style: TextStyle) {
        textStyle = style
    }

    fun placeholderColor(color: Color) {
        placeholderColor = color
    }

    fun iconColor(color: Color) {
        iconColor = color
    }

    fun cursorColor(color: Color) {
        cursorColor = color
    }

    fun iconSize(size: Dp) {
        iconSize = size
    }

    fun iconInset(inset: Dp) {
        iconInset = inset
    }
}
