package com.xbot.kotogram.feature.connection.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.xbot.kotogram.feature.connection.domain.repository.ConnectionRepository
import com.xbot.kotogram.feature.connection.impl.platform.rememberAppForeground
import org.koin.compose.koinInject

@Composable
fun ConnectionEffect(repository: ConnectionRepository = koinInject()) {
    val inForeground = rememberAppForeground()
    LaunchedEffect(repository, inForeground) {
        snapshotFlow { inForeground.value }.collect { repository.setInForeground(it) }
    }
    DisposableEffect(repository) {
        onDispose {
            repository.setInForeground(false)
        }
    }
}
