package com.xbot.kotogram.feature.auth.presentation.screen.premiumpurchase

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xbot.kotogram.core.designsystem.KotogramPreview
import com.xbot.kotogram.feature.auth.presentation.component.AuthScaffold
import com.xbot.kotogram.feature.auth.presentation.component.AuthSubtitle
import com.xbot.kotogram.feature.auth.presentation.resources.Res
import com.xbot.kotogram.feature.auth.presentation.resources.action_back
import com.xbot.kotogram.feature.auth.presentation.resources.unsupported_subtitle
import com.xbot.kotogram.feature.auth.presentation.resources.unsupported_title
import org.jetbrains.compose.resources.stringResource

/** TDLib asks for a Telegram Premium purchase to log in, which needs the official app's store billing. */
@Composable
internal fun PremiumPurchaseScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    AuthScaffold(
        title = stringResource(Res.string.unsupported_title),
        subtitle = { AuthSubtitle(stringResource(Res.string.unsupported_subtitle)) },
        modifier = modifier
    ) {
        TextButton(onClick = onBackClick) { Text(stringResource(Res.string.action_back)) }
    }
}

@KotogramPreview
@Composable
private fun PremiumPurchaseScreenPreview() {
    PremiumPurchaseScreen(onBackClick = {})
}
