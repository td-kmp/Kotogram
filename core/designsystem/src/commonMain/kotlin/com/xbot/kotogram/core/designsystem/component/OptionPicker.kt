package com.xbot.kotogram.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.theme.currentPlatform
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

@Composable
fun <T> OptionPicker(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<T>,
    optionText: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    selected: T? = null,
    searchPlaceholder: String = "",
    matches: (T, String) -> Boolean = { option, query -> optionText(option).contains(query, ignoreCase = true) }
) {
    if (currentPlatform.isDesktop) {
        Menu(expanded, onDismissRequest, options, optionText, onSelect, modifier, selected)
    } else if (expanded) {
        OptionSheet(onDismissRequest, options, optionText, onSelect, selected, searchPlaceholder, matches)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionSheet(
    onDismissRequest: () -> Unit,
    options: List<T>,
    optionText: (T) -> String,
    onSelect: (T) -> Unit,
    selected: T?,
    searchPlaceholder: String,
    matches: (T, String) -> Boolean
) {
    val query = rememberTextFieldState()
    ModalBottomSheet(onDismissRequest = onDismissRequest, containerColor = KotogramTheme.colors.background) {
        TextField(
            state = query,
            placeholder = { Text(searchPlaceholder) },
            leadingIcon = { Icon(KotogramIcons.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = KotogramTheme.spacing.large)
        )
        val text = query.text.toString()
        LazyColumn {
            items(options.filter { text.isBlank() || matches(it, text) }) { option ->
                ListItem(
                    headlineContent = { Text(optionText(option)) },
                    trailingContent = if (option == selected) {
                        { Icon(KotogramIcons.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    onClick = {
                        onSelect(option)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
