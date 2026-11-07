package com.xbot.kotogram.feature.auth.impl.screen.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.component.ListItem
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.searchfield_search
import com.xbot.kotogram.core.resources.stringResource
import io.xbot.tdlib.api.CountryInfo

@Composable
internal fun CountryPickerDialog(
    query: String,
    countries: List<CountryInfo>,
    onQueryChange: (String) -> Unit,
    onSelect: (CountryInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(Res.string.searchfield_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(countries, key = {
                        it.countryCode
                    }, contentType = { "country" }) { country ->
                        ListItem(
                            headlineContent = { Text("${country.flagEmoji} ${country.name}") },
                            trailingContent = {
                                Text("+${country.callingCodes.firstOrNull().orEmpty()}")
                            },
                            onClick = { onSelect(country) }
                        )
                    }
                }
            }
        }
    )
}
