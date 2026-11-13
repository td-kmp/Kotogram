package com.xbot.kotogram.feature.connection.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.xbot.kotogram.feature.connection.domain.repository.ConnectionRepository
import com.xbot.kotogram.feature.connection.impl.platform.isAppInForeground
import org.koin.compose.koinInject

@Composable
fun ConnectionEffect(
    inForeground: Boolean = isAppInForeground(),
    repository: ConnectionRepository = koinInject(),
) {
    LaunchedEffect(repository, inForeground) {
        repository.setInForeground(inForeground)
    }
    DisposableEffect(repository) {
        onDispose {
            repository.setInForeground(false)
        }
    }
}
