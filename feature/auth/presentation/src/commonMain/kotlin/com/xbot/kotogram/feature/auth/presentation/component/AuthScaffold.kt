package com.xbot.kotogram.feature.auth.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Shared layout of the login screens: a centered column with a title and a subtitle. */
@Composable
internal fun AuthScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            subtitle?.invoke()
            content()
        }
    }
}

@Composable
internal fun AuthSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
internal fun AuthErrorText(text: String?) {
    if (text !=
        null
    ) {
        Text(
            text,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
