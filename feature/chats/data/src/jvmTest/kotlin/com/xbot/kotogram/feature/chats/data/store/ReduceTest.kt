package com.xbot.kotogram.feature.chats.data.store

import com.xbot.kotogram.feature.chats.data.extensions.reduce
import io.xbot.tdlib.api.AuthorizationStateReady
import io.xbot.tdlib.api.Chat
import io.xbot.tdlib.api.ChatList
import io.xbot.tdlib.api.ChatListArchive
import io.xbot.tdlib.api.ChatListMain
import io.xbot.tdlib.api.ChatPosition
import io.xbot.tdlib.api.Update
import io.xbot.tdlib.api.UpdateAuthorizationState
import io.xbot.tdlib.api.UpdateChatLastMessage
import io.xbot.tdlib.api.UpdateChatPosition
import io.xbot.tdlib.api.UpdateChatTitle
import io.xbot.tdlib.api.UpdateNewChat
import io.xbot.tdlib.api.parseTdObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ReduceTest {
    private fun chat(
        id: Long,
        vararg positions: ChatPosition,
    ): Chat {
        val json = """{"@type":"chat","id":$id,"title":"chat $id","type":{"@type":"chatTypePrivate","user_id":$id},
            |"permissions":{"@type":"chatPermissions"},"notification_settings":{"@type":"chatNotificationSettings"},
            |"available_reactions":{"@type":"chatAvailableReactionsAll"},"video_chat":{"@type":"videoChat"}}
        """.trimMargin()
        return (parseTdObject(json) as Chat).copy(positions = positions.toList())
    }

    private fun position(
        list: ChatList,
        order: Long,
    ) = ChatPosition(list = list, order = order)

    private fun fold(vararg updates: Update) = updates.fold(ChatsState.Empty) { state, update -> state.reduce(update) }

    private fun ChatsState.main() = chatLists[ChatListMain]?.chatIds.orEmpty()

    @Test
    fun chatsSortByOrderThenIdDescending() {
        val state = fold(
            UpdateNewChat(chat(1, position(ChatListMain, 10))),
            UpdateNewChat(chat(2, position(ChatListMain, 30))),
            UpdateNewChat(chat(3, position(ChatListMain, 10))),
        )
        assertEquals(listOf(2L, 3L, 1L), state.main())
    }

    @Test
    fun orderZeroRemovesTheChatFromThatListOnly() {
        val state = fold(
            UpdateNewChat(chat(1, position(ChatListMain, 10), position(ChatListArchive, 20))),
            UpdateChatPosition(chatId = 1, position = position(ChatListMain, 0)),
        )
        assertEquals(emptyList(), state.main())
        assertEquals(listOf(1L), state.chatLists[ChatListArchive]?.chatIds)
        assertEquals(listOf(ChatListArchive), state.chats[1]?.positions?.map { it.list })
        assertEquals(1L, state.chats[1]?.id)
    }

    @Test
    fun aLastMessageUpdateCarriesTheWholeSetOfPositions() {
        val state = fold(
            UpdateNewChat(chat(1, position(ChatListMain, 10))),
            UpdateNewChat(chat(2, position(ChatListMain, 20))),
            UpdateChatLastMessage(
                chatId = 1,
                lastMessage = null,
                positions = listOf(position(ChatListMain, 30)),
            ),
        )
        assertEquals(listOf(1L, 2L), state.main())
    }

    @Test
    fun aListMissingFromThoseIsLeft() {
        val state = fold(
            UpdateNewChat(chat(1, position(ChatListMain, 10), position(ChatListArchive, 20))),
            UpdateChatLastMessage(
                chatId = 1,
                lastMessage = null,
                positions = listOf(position(ChatListMain, 10)),
            ),
        )
        assertEquals(emptyList(), state.chatLists[ChatListArchive]?.chatIds)
        assertEquals(listOf(1L), state.main())
    }

    @Test
    fun anUpdateForAnotherChatKeepsThisChatTheSameInstance() {
        val before = fold(UpdateNewChat(chat(1)), UpdateNewChat(chat(2)))
        val after = before.reduce(UpdateChatTitle(chatId = 2, title = "new"))
        assertSame(before.chats[1], after.chats[1])
        assertNotSame(before.chats[2], after.chats[2])
        assertEquals("new", after.chats[2]?.title)
    }

    @Test
    fun anUpdateItDoesNotHandleChangesNothing() {
        val state = fold(UpdateNewChat(chat(1)))
        assertSame(state, state.reduce(UpdateAuthorizationState(AuthorizationStateReady)))
    }

    @Test
    fun aFieldUpdateForAnUnknownChatIsIgnored() {
        val state = ChatsState.Empty
        assertSame(state, state.reduce(UpdateChatTitle(chatId = 9, title = "x")))
    }
}
