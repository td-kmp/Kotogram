package com.xbot.kotogram.ui

// Placeholder data until the screens are backed by TDLib.
internal data class SampleChat(
    val id: Long,
    val title: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0
)

internal data class SampleMessage(val text: String, val isOutgoing: Boolean)

internal val sampleChats = listOf(
    SampleChat(1, "Saved Messages", "tdlib-kmp 1.8.68-1.0.0-alpha01 🎉", "20:14"),
    SampleChat(2, "Kotlin", "Kotlin 2.4.20 is out", "18:02", unreadCount = 3),
    SampleChat(3, "Compose Multiplatform", "1.13.0-alpha01 release notes", "Yesterday"),
    SampleChat(4, "Telegram", "Login code: 12345", "Mon", unreadCount = 1)
)

internal val sampleMessages = listOf(
    SampleMessage("Hi! This is Kotogram.", isOutgoing = false),
    SampleMessage("Telegram client on Kotlin Multiplatform", isOutgoing = true),
    SampleMessage("Android, JVM and iOS from one codebase", isOutgoing = false)
)
