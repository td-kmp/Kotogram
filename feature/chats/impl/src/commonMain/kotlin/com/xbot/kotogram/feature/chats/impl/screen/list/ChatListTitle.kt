package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.xbot.kotogram.core.designsystem.component.ProgressIndicator
import com.xbot.kotogram.core.designsystem.component.ProgressIndicatorStyle
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.extensions.title
import com.xbot.kotogram.core.resources.peerlist_title_chats
import com.xbot.kotogram.core.resources.stringResource
import io.xbot.tdlib.api.ConnectionState

@Composable
context(style: ChatListScreenStyleScope)
internal fun ChatListTitle(
    connection: ConnectionState?,
    modifier: Modifier = Modifier,
) {
    val status = connection?.title
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(style.connectionIndicatorGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (status != null) {
            ProgressIndicator(
                style = ProgressIndicatorStyle {
                    size(style.connectionIndicatorSize)
                    color(style.connectionIndicatorColor)
                },
            )
        }
        Text(
            text = if (status != null) stringResource(status) else stringResource(Res.string.peerlist_title_chats),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
