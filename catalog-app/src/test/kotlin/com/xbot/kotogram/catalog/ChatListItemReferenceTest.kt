package com.xbot.kotogram.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import com.xbot.kotogram.core.designsystem.KotogramTheme
import com.xbot.kotogram.core.designsystem.component.Avatar
import com.xbot.kotogram.core.designsystem.component.AvatarBadge
import com.xbot.kotogram.core.designsystem.component.AvatarShape
import com.xbot.kotogram.core.designsystem.component.Badges
import com.xbot.kotogram.core.designsystem.component.ChatAvatar
import com.xbot.kotogram.core.designsystem.component.ChatListItem
import com.xbot.kotogram.core.designsystem.component.ChatListItemSeparator
import com.xbot.kotogram.core.designsystem.component.Date
import com.xbot.kotogram.core.designsystem.component.ForumTopic
import com.xbot.kotogram.core.designsystem.component.Message
import com.xbot.kotogram.core.designsystem.component.MessageIcon
import com.xbot.kotogram.core.designsystem.component.MessageStatus
import com.xbot.kotogram.core.designsystem.component.MessageThumbnail
import com.xbot.kotogram.core.designsystem.component.MessageThumbnailType
import com.xbot.kotogram.core.designsystem.component.Title
import com.xbot.kotogram.core.designsystem.component.TitleMark
import com.xbot.kotogram.core.designsystem.component.TopicIcon
import com.xbot.kotogram.core.designsystem.style.ProvideFormFactor
import org.junit.Assume.assumeTrue
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.test.Test

class ChatListItemReferenceTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun compareWithTelegramForIos() {
        val root = File(REFERENCES)
        assumeTrue("No references in ${root.absolutePath}", root.isDirectory)
        val out = File(OUTPUT).apply { mkdirs() }
        val report = buildString {
            ReferenceRows.forEach { (folder, rows) ->
                rows.filter { File(root, "$folder/${it.file}.png").isFile }.forEach { row ->
                    val reference = ImageIO.read(File(root, "$folder/${row.file}.png"))
                    val images = ReferenceImages(root, folder, row)
                    var ours: BufferedImage? = null
                    runDesktopComposeUiTest(width = reference.width, height = reference.height) {
                        mainClock.autoAdvance = false
                        setContent {
                            CompositionLocalProvider(LocalDensity provides Density(reference.width / WIDTH_POINTS)) {
                                KotogramTheme(darkTheme = folder == DARK, dynamicColor = false) {
                                    ProvideFormFactor(laptop = false) { ReferenceRow(row, images) }
                                }
                            }
                        }
                        mainClock.advanceTimeBy(SETTLE_MILLIS)
                        ours = onRoot().captureToImage().toAwtImage()
                    }
                    val result = compare(ours!!, reference, File(out, "$folder-${row.file}.png"))
                    appendLine("$folder/${row.file}: $result")
                }
            }
        }
        println(report)
        File(out, "report.txt").writeText(report)
    }
}

private class ReferenceImages(
    root: File,
    folder: String,
    row: ReferenceRow,
) {
    val avatar: ImageBitmap = ImageIO.read(File(root, "$folder/${row.file}.avatar.png")).toComposeImageBitmap()
    val thumbnail: ImageBitmap? = File(root, "$folder/${row.file}.thumbnail.png")
        .takeIf { it.isFile }
        ?.let { ImageIO.read(it).toComposeImageBitmap() }
}

@Composable
private fun ReferenceRow(
    row: ReferenceRow,
    images: ReferenceImages,
) {
    ChatListItem(
        avatar = {
            ChatAvatar(
                badge = if (row.online) AvatarBadge.Online else null,
                shape = if (row.forum) AvatarShape.RoundedRect else AvatarShape.Circle,
            ) {
                if (row.forum) {
                    Image(images.avatar, null, Modifier.fillMaxSize())
                } else if (row.letters != null) {
                    Avatar(
                        photo = null,
                        letters = row.letters,
                        colorIndex = row.colorIndex,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        images.avatar,
                        null,
                        Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        },
        title = { Title(row.title, mark = if (row.verified) TitleMark.Verified else null, muted = row.muted) },
        date = { Date(row.date, status = row.status) },
        text = {
            Message(
                text = AnnotatedString(row.text),
                author = row.author,
                icon = row.icon,
                thumbnails = thumbnails(row, images.thumbnail),
                topics = row.topics,
            )
        },
        badges = {
            Badges(
                unreadCount = row.unread,
                muted = row.muted,
                mention = row.mention,
                reaction = row.reaction,
                pinned = row.pinned,
            )
        },
        pinned = row.pinned,
        separator = row.separator,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun thumbnails(
    row: ReferenceRow,
    image: ImageBitmap?,
): List<MessageThumbnail> {
    if (image != null) {
        return listOf(MessageThumbnail({ Image(image, null, Modifier.fillMaxSize()) }, row.thumbnailType))
    }
    return List(row.thumbnails) { MessageThumbnail({ Box(Modifier.fillMaxSize().background(Color.Gray)) }) }
}

private fun compare(
    ours: BufferedImage,
    reference: BufferedImage,
    out: File,
): String {
    val width = minOf(ours.width, reference.width)
    val height = minOf(ours.height, reference.height)
    val sheet = BufferedImage(width, height * 4, BufferedImage.TYPE_INT_RGB)
    var differing = 0
    var inkMismatch = 0
    var ink = 0
    val ourBackground = ours.getRGB(width - 4, 4)
    val referenceBackground = reference.getRGB(width - 4, 4)
    for (y in 0 until height - 1) {
        for (x in 0 until width) {
            val a = ours.getRGB(x, y)
            val b = reference.getRGB(x, y)
            val delta = distance(a, b)
            if (delta > THRESHOLD) differing++
            val ourInk = distance(a, ourBackground) > INK
            val referenceInk = distance(b, referenceBackground) > INK
            if (ourInk || referenceInk) ink++
            if (ourInk != referenceInk) inkMismatch++
            sheet.setRGB(x, y, a)
            sheet.setRGB(x, y + height, b)
            sheet.setRGB(
                x,
                y + height * 2,
                if (delta >
                    THRESHOLD
                ) rgb(255, 255 - delta, 255 - delta) else rgb(32, 32, 32),
            )
            sheet.setRGB(x, y + height * 3, rgb(luma(a), luma(b), luma(b)))
        }
    }
    ImageIO.write(sheet, "png", out)
    val percent = differing * 100.0 / (width * height)
    val shape = inkMismatch * 100.0 / ink.coerceAtLeast(1)
    return "shape %.2f%% of the ink, colour %.2f%% of the pixels".format(shape, percent)
}

private fun red(color: Int) = color shr 16 and 0xFF

private fun green(color: Int) = color shr 8 and 0xFF

private fun blue(color: Int) = color and 0xFF

private fun distance(
    a: Int,
    b: Int,
) = maxOf(abs(red(a) - red(b)), abs(green(a) - green(b)), abs(blue(a) - blue(b)))

private fun luma(color: Int) = (red(color) * 299 + green(color) * 587 + blue(color) * 114) / 1000

private fun rgb(
    r: Int,
    g: Int,
    b: Int,
) = (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)

private data class ReferenceRow(
    val file: String,
    val title: String,
    val date: String,
    val text: String,
    val author: String? = null,
    val status: MessageStatus? = null,
    val unread: String? = null,
    val muted: Boolean = false,
    val verified: Boolean = false,
    val mention: Boolean = false,
    val reaction: Boolean = false,
    val online: Boolean = false,
    val pinned: Boolean = false,
    val icon: MessageIcon? = null,
    val thumbnails: Int = 0,
    val thumbnailType: MessageThumbnailType = MessageThumbnailType.Photo,
    val separator: ChatListItemSeparator = ChatListItemSeparator.Inset,
    val letters: String? = null,
    val colorIndex: Int = 0,
    val forum: Boolean = false,
    val topics: List<ForumTopic> = emptyList(),
)

private val MamaFirst = ReferenceRow("0-mama-pinned-1", "Мама", "18:19", "Немного подожди", pinned = true)
private val MamaSecond = ReferenceRow(
    file = "1-mama-pinned-2",
    title = "Мама",
    date = "13/10",
    text = "CamScanner 29-06-26 15.03.pdf",
    pinned = true,
    icon = MessageIcon.Forwarded,
    separator = ChatListItemSeparator.FullWidth,
)
private val Troparevo = ReferenceRow(
    file = "troparevo",
    title = "Тропарево-Никулино Общественный совет района",
    date = "20:27",
    author = "ЭкоКонтроль | Москва",
    text = "Спасибо за сигнал, принято!",
    unread = "6,6K",
    muted = true,
)
private val Rozetked = ReferenceRow(
    file = "rozetked",
    title = "Rozetked Discuss",
    date = "20:26",
    author = "Илья Фѣдоровъ",
    text = "да кому верить то йоптваю мать",
    unread = "217,6K",
    muted = true,
)
private val Botalka = ReferenceRow(
    file = "botalka",
    title = "Боталка(Студенты) — Последние новости",
    date = "20:26",
    author = "Johan",
    text = "а чё так, может прокнет в след раз?)",
    unread = "4,7K",
    muted = true,
)
private val TudaSyuda = ReferenceRow(
    file = "tudasyuda",
    title = "ТудаСюда UZ | Места и события Ташкента",
    date = "20:25",
    text = "🛍 🔥 Товары от 10 000 сумов: в Ташкенте открылся новый огромный магазин с подарками",
    unread = "337",
    muted = true,
    thumbnails = 1,
)
private val Sber = ReferenceRow(
    file = "sber",
    title = "Сбер",
    date = "20:08",
    text = "ТЕМА УРОКА — ИСКУССТВЕННЫЙ ИНТЕЛЛЕКТ 🤖 и как он меняет обучение школьников",
    unread = "465",
    muted = true,
    verified = true,
    thumbnails = 3,
)

private const val GROUP = "Javokhir и ."
private const val LINK = "https://t.me/+7N7g7L8o5T1iOTky"
private const val YOU = "Вы"
private const val ORANGE_TOPIC = 0xFFD67E
private const val VIOLET_TOPIC = 0xCB86DB

private val ReferenceRows = listOf(
    LIGHT to listOf(
        MamaFirst,
        MamaSecond,
        ReferenceRow(
            file = "2-novosti-moskvy",
            title = "Новости Москвы",
            date = "20:28",
            text = "🏝 Ловите бооооольшую волну скидок на 11.11! На Островке запустили распродажу",
            unread = "10",
            muted = true,
            verified = true,
            thumbnails = 1,
        ),
        Troparevo.copy(file = "3-troparevo"),
        Rozetked.copy(file = "4-rozetked"),
        Botalka.copy(file = "5-botalka"),
        TudaSyuda.copy(file = "6-tudasyuda"),
        Sber.copy(file = "7-sber"),
    ),
    DARK to listOf(
        MamaFirst,
        MamaSecond,
        Troparevo.copy(file = "2-troparevo"),
        Rozetked.copy(file = "3-rozetked"),
        Botalka.copy(file = "4-botalka"),
        TudaSyuda.copy(file = "5-tudasyuda"),
        Sber.copy(file = "6-sber"),
        ReferenceRow(
            file = "7-4ch",
            title = "4ch",
            date = "20:03",
            text = "Это реально того стоит? 😢 4ch",
            unread = "183",
            muted = true,
            thumbnails = 1,
        ),
        ReferenceRow(
            file = "8-forum",
            title = "ФЛ-1",
            date = "10:32",
            author = "Victoria",
            text = "P100027580-7 - Apple Pay & Google Pay",
            unread = "1",
            muted = true,
            icon = MessageIcon.Forwarded,
            thumbnails = 1,
            forum = true,
            topics = listOf(
                ForumTopic("Apple Pay & Google Pay", TopicIcon.Bubble(ORANGE_TOPIC), unread = true),
                ForumTopic("Visa Infinite", TopicIcon.Bubble(VIOLET_TOPIC)),
            ),
        ),
    ),
    LIGHT_SMALL to listOf(
        ReferenceRow(
            "20-mention",
            GROUP,
            "21:20",
            "@xephosbot ну че там",
            author = ".",
            unread = "2",
            mention = true,
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            "21-link-read",
            ".",
            "21:19",
            LINK,
            status = MessageStatus.Read,
            muted = true,
            letters = ".",
            colorIndex = 3,
        ),
        ReferenceRow(
            "22-reaction",
            GROUP,
            "21:20",
            "Хей",
            author = YOU,
            status = MessageStatus.Read,
            reaction = true,
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            "23-sending",
            GROUP,
            "21:21",
            "Отправляется",
            author = YOU,
            status = MessageStatus.Sending,
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            "24-sent",
            GROUP,
            "21:21",
            "Отправляется",
            author = YOU,
            status = MessageStatus.Sent,
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            "25-online",
            ".",
            "21:19",
            LINK,
            status = MessageStatus.Read,
            muted = true,
            online = true,
            letters = ".",
            colorIndex = 3,
        ),
        ReferenceRow(
            file = "26-voice",
            title = GROUP,
            date = "21:22",
            text = "Голосовое сообщение",
            author = "Javokhir Isaev",
            icon = MessageIcon.VoiceMessage,
            unread = "2",
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            "27-link-private",
            "Javokhir Isaev",
            "21:19",
            LINK,
            letters = "JI",
            colorIndex = 6,
        ),
        ReferenceRow("28-photo", "ANIKA", "21:17", "Фотография", unread = "171", muted = true),
        ReferenceRow(
            file = "29-video-message",
            title = GROUP,
            date = "21:22",
            text = "Видеосообщение",
            author = ".",
            unread = "1",
            thumbnailType = MessageThumbnailType.RoundVideo,
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            file = "30-missed-call",
            title = ".",
            date = "21:23",
            text = "Пропущенный звонок",
            icon = MessageIcon.IncomingCall,
            unread = "1",
            muted = true,
            letters = ".",
            colorIndex = 3,
        ),
        ReferenceRow(
            "31-document",
            GROUP,
            "21:23",
            "gib-attributes-packet-20261019.log",
            author = YOU,
            status = MessageStatus.Read,
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            file = "32-location",
            title = GROUP,
            date = "21:24",
            text = "Геопозиция",
            author = YOU,
            status = MessageStatus.Sent,
            icon = MessageIcon.Location,
            letters = "J",
            colorIndex = 3,
        ),
        ReferenceRow(
            "33-poll",
            GROUP,
            "21:24",
            "Лоли",
            author = YOU,
            status = MessageStatus.Sent,
            icon = MessageIcon.Poll,
            letters = "J",
            colorIndex = 3,
        ),
    ),
)

private const val REFERENCES = "references/chat-list-item"
private const val OUTPUT = "build/chat-list-item-reference"
private const val LIGHT = "light"
private const val DARK = "dark"
private const val LIGHT_SMALL = "light-small"
private const val WIDTH_POINTS = 420f
private const val SETTLE_MILLIS = 1_000L
private const val THRESHOLD = 40
private const val INK = 24
