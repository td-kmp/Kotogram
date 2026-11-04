package com.xbot.kotogram.shared.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.designsystem.KotogramPreview

/** Shown until TDLib reports the active account's state, and while it restarts after a log out. */
@Composable
internal fun SplashScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@KotogramPreview
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
