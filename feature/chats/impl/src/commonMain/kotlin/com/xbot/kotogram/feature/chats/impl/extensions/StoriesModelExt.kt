package com.xbot.kotogram.feature.chats.impl.extensions

import androidx.compose.runtime.Composable
import com.xbot.kotogram.core.designsystem.component.StoryRing
import com.xbot.kotogram.core.resources.Res
import com.xbot.kotogram.core.resources.gallery_pagecontroller_live
import com.xbot.kotogram.core.resources.stringResource
import com.xbot.kotogram.feature.chats.impl.screen.list.StoriesModel

@Composable
internal fun StoriesModel.toRing(): StoryRing = when (this) {
    is Stories -> StoryRing.Stories(total, unseen, closeFriends)
    Live -> StoryRing.Live(stringResource(Res.string.gallery_pagecontroller_live))
}
