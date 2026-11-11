package com.xbot.kotogram.feature.chats.impl.screen.list

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.media.LocalUiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.KeyboardKind
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.PointerPrecision
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.ViewingDistance
import com.xbot.kotogram.core.designsystem.media.UiMediaScope.WindowPosture
import com.xbot.kotogram.core.designsystem.style.LocalMediaQueryInfo
import com.xbot.kotogram.core.designsystem.style.MediaQueryInfo
import com.xbot.kotogram.feature.chats.domain.model.ChatFolderTab
import io.xbot.tdlib.api.ChatFolderIcon
import io.xbot.tdlib.api.ChatFolderInfo
import io.xbot.tdlib.api.ChatFolderName
import io.xbot.tdlib.api.ChatListFolder
import io.xbot.tdlib.api.ChatListMain
import io.xbot.tdlib.api.FormattedText
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class ChatListScreenScreenshotTest {
    @Test
    fun iphoneAirDark() = render("iphone-air_dark", Phone, laptop = false, scale = 3)

    @Test
    fun laptopDark() = render("laptop_dark", Laptop, laptop = true, scale = 2)

    @OptIn(ExperimentalTestApi::class)
    private fun render(
        name: String,
        media: UiMediaScope,
        laptop: Boolean,
        scale: Int,
    ) = runDesktopComposeUiTest(
        width = (media.windowWidth.value * scale).toInt(),
        height = (media.windowHeight.value * scale).toInt(),
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(scale.toFloat())) {
                KotogramTheme(darkTheme = true, dynamicColor = false) {
                    CompositionLocalProvider(
                        LocalUiMediaScope provides media,
                        LocalMediaQueryInfo provides MediaQueryInfo(isLaptop = laptop, isTv = false, isAuto = false),
                    ) {
                        ChatListScreenContent(state = sampleState(), onAction = {}, onChatClick = {})
                    }
                }
            }
        }
        Thread.sleep(IMAGE_WAIT_MILLIS)
        mainClock.advanceTimeBy(IMAGE_WAIT_MILLIS)
        waitForIdle()
        val file = File("build/screenshots/chat-list/$name.png")
        file.parentFile.mkdirs()
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", file)
    }
}

private fun sampleState(): ChatListScreenState {
    val personal = ChatFolderInfo(
        id = 1,
        name = ChatFolderName(text = FormattedText(text = "Personal")),
        icon = ChatFolderIcon(name = "Private"),
    )
    val photos = listOf(0xFF6D4C41, 0xFF1E88E5, 0xFF37474F, 0xFFC62828, 0xFF2E7D32, 0xFFF9A825, 0xFF455A64)
    return ChatListScreenState(
        tabs = listOf(
            ChatFolderTab(ChatListMain, folder = null, unreadCount = 0, hasUnmutedUnread = false),
            ChatFolderTab(ChatListFolder(chatFolderId = 1), personal, unreadCount = 8, hasUnmutedUnread = true),
        ),
        rows = SampleRows.mapIndexed { index, row ->
            row.withPhoto(if (index == 6) null else photo(photos[index]))
                .withThumbnail(if (index == 3 || index == 4) photo(0xFF90A4AE) else null)
        },
    )
}

private fun ChatListRow.withPhoto(photo: ByteArray?) =
    copy(avatar = AvatarModel(null, photo, avatar.letters, avatar.colorIndex))

private fun ChatListRow.withThumbnail(thumbnail: ByteArray?) = copy(
    preview = (preview as? ChatPreview.Media)?.let { ChatPreview.Media(it.kind, it.text, thumbnail) } ?: preview,
)

private fun ChatListRow.copy(
    avatar: AvatarModel = this.avatar,
    preview: ChatPreview = this.preview,
) = ChatListRow(
    chatId, title, avatar, emojiStatusId, isMuted, isPinned, time, outgoing, unreadCount, hasUnreadMention,
    isMarkedAsUnread, senderName, preview,
)

private fun photo(argb: Long): ByteArray {
    val surface = Surface.makeRasterN32Premul(PHOTO_SIDE, PHOTO_SIDE)
    surface.canvas.drawPaint(Paint().apply { color = argb.toInt() })
    val figure = Paint().apply { color = Color.makeARGB(110, 255, 255, 255) }
    surface.canvas.drawCircle(PHOTO_SIDE * 0.5f, PHOTO_SIDE * 0.42f, PHOTO_SIDE * 0.2f, figure)
    surface.canvas.drawCircle(PHOTO_SIDE * 0.5f, PHOTO_SIDE * 1.0f, PHOTO_SIDE * 0.38f, figure)
    return requireNotNull(surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)).bytes
}

private val Phone = media(PHONE_WIDTH, PHONE_HEIGHT, laptop = false)

private val Laptop = media(LAPTOP_WIDTH, LAPTOP_HEIGHT, laptop = true)

private fun media(
    width: Int,
    height: Int,
    laptop: Boolean,
) = object : UiMediaScope {
    override val windowPosture = WindowPosture(emptyList())
    override val windowWidth = width.dp
    override val windowHeight = height.dp
    override val pointerPrecision = if (laptop) PointerPrecision.Fine else PointerPrecision.Coarse
    override val keyboardKind = if (laptop) KeyboardKind.Physical else KeyboardKind.Virtual
    override val hasMicrophone = false
    override val hasCamera = false
    override val viewingDistance = ViewingDistance.Near
}

private const val PHONE_WIDTH = 420
private const val PHONE_HEIGHT = 912
private const val LAPTOP_WIDTH = 380
private const val LAPTOP_HEIGHT = 640
private const val PHOTO_SIDE = 120
private const val IMAGE_WAIT_MILLIS = 1500L
