package com.xbot.kotogram.core.designsystem

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

/** Light and dark previews in [KotogramTheme]; the preview function shows only its content. */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@PreviewLightDark
@PreviewWrapper(KotogramPreviewWrapper::class)
annotation class KotogramPreview

internal class KotogramPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        KotogramTheme { Surface(Modifier.fillMaxSize(), content = content) }
    }
}
