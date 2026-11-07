package com.xbot.kotogram.catalog.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.xbot.kotogram.catalog.model.CatalogExample
import com.xbot.kotogram.catalog.model.CatalogPage
import com.xbot.kotogram.catalog.model.CatalogSection
import com.xbot.kotogram.core.designsystem.component.SecureTextField
import com.xbot.kotogram.core.designsystem.icon.KotogramIcons

internal val SecureTextFieldPage = CatalogPage(
    id = "secure-text-field",
    section = CatalogSection.Components,
    name = "SecureTextField",
    description = "The password field of Telegram for macOS's sign-in (Auth_PasswordEntry): the same field over " +
        "BasicSecureTextField, with a button that shows the password.",
    examples = listOf(
        CatalogExample("Hidden") { PasswordField(revealed = false) },
        CatalogExample("Shown") { PasswordField(revealed = true) },
        CatalogExample("Empty") {
            SecureTextField(state = rememberTextFieldState(), placeholder = { Text("Your password") })
        },
        CatalogExample("Error") {
            SecureTextField(
                state = rememberTextFieldState("kotogram1"),
                placeholder = { Text("Your password") },
                isError = true,
                supportingText = { Text("Invalid password") },
                textObfuscationMode = TextObfuscationMode.Hidden
            )
        }
    )
)

@Composable
private fun PasswordField(revealed: Boolean) {
    var shown by remember { mutableStateOf(revealed) }
    SecureTextField(
        state = rememberTextFieldState("kotogram"),
        placeholder = { Text("Your password") },
        textObfuscationMode = if (shown) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
        trailingIcon = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable { shown = !shown },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (shown) KotogramIcons.Eye else KotogramIcons.EyeOff,
                    contentDescription = if (shown) "Hide password" else "Show password"
                )
            }
        }
    )
}
