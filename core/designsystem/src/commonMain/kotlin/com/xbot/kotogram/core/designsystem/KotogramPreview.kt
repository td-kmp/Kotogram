package com.xbot.kotogram.core.designsystem

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import com.xbot.kotogram.core.designsystem.style.ProvideFormFactor

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@PreviewLightDark
@PreviewWrapper(KotogramPreviewWrapper::class)
annotation class KotogramPreview

// Workaround: Studio wraps a function's every preview in its first PreviewWrapper, so the laptop one is its own.
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(name = "Laptop", device = LAPTOP_DEVICE)
@Preview(name = "Laptop, dark", device = LAPTOP_DEVICE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@PreviewWrapper(KotogramLaptopPreviewWrapper::class)
annotation class KotogramLaptopPreview

internal class KotogramPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        KotogramPreviewTheme(laptop = false, content)
    }
}

internal class KotogramLaptopPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        KotogramPreviewTheme(laptop = true, content)
    }
}

@Composable
private fun KotogramPreviewTheme(
    laptop: Boolean,
    content: @Composable () -> Unit,
) {
    KotogramTheme(dynamicColor = false) {
        ProvideFormFactor(laptop) { Surface(Modifier.fillMaxSize(), content = content) }
    }
}

private const val LAPTOP_DEVICE = "spec:width=700dp,height=550dp,dpi=160"
