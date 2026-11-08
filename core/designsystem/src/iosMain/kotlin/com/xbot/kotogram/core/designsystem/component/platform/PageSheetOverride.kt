package com.xbot.kotogram.core.designsystem.component.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.BottomSheetOverride
import com.xbot.kotogram.core.designsystem.component.BottomSheetOverrideScope
import com.xbot.kotogram.core.designsystem.component.DefaultBottomSheetScope
import com.xbot.kotogram.core.designsystem.extensions.isDark
import com.xbot.kotogram.core.designsystem.extensions.toUIColor
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.presentationController
import platform.UIKit.sheetPresentationController
import platform.darwin.NSObject

// A page sheet hosting the content in a composition of its own, as Calf's AdaptiveBottomSheet does.
internal object PageSheetOverride : BottomSheetOverride {
    @Composable
    override fun BottomSheetOverrideScope.BottomSheet() {
        val parent = LocalUIViewController.current
        val locals = rememberUpdatedState(currentCompositionLocalContext)
        val sheetContent = rememberUpdatedState(content)
        val sheetModifier = rememberUpdatedState(modifier)
        val sheetStyle = rememberUpdatedState(style)
        val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
        val dark = KotogramTheme.colors.background.isDark()
        val sheet = remember {
            ComposeUIViewController {
                // The sheet's own platform locals (window, focus, text input) go over the screen's.
                val own = currentCompositionLocalContext
                CompositionLocalProvider(locals.value) {
                    CompositionLocalProvider(own) {
                        CompositionLocalProvider(LocalContentColor provides sheetStyle.value.contentColor) {
                            Column(
                                modifier = sheetModifier.value
                                    .fillMaxSize()
                                    .background(sheetStyle.value.containerColor)
                            ) {
                                // The sheet's own controller: UIKit slides it down, then the caller drops it.
                                val controller = LocalUIViewController.current
                                val scope = DefaultBottomSheetScope(this) {
                                    controller.dismissViewControllerAnimated(true) { currentOnDismissRequest() }
                                }
                                sheetContent.value(scope)
                            }
                        }
                    }
                }
            }
        }
        val delegate = remember { SheetDismissDelegate { currentOnDismissRequest() } }
        SideEffect {
            sheet.view.backgroundColor = style.containerColor.toUIColor()
            sheet.overrideUserInterfaceStyle = interfaceStyle(dark)
        }
        DisposableEffect(sheet) {
            sheet.modalPresentationStyle = UIModalPresentationPageSheet
            sheet.sheetPresentationController?.detents = listOf(UISheetPresentationControllerDetent.largeDetent())
            sheet.presentationController?.delegate = delegate
            parent.presentViewController(sheet, animated = true, completion = null)
            onDispose {
                if (sheet.presentingViewController != null) sheet.dismissViewControllerAnimated(true, completion = null)
            }
        }
    }
}

private class SheetDismissDelegate(private val onDismiss: () -> Unit) :
    NSObject(),
    UIAdaptivePresentationControllerDelegateProtocol {
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        onDismiss()
    }
}
