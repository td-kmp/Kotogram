package com.xbot.kotogram.core.account

import io.xbot.tdlib.TdLib
import io.xbot.tdlib.TdLogMessageHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class FakeTdLib(
    private val startStates: Map<Int, List<String>> = emptyMap(),
    private val userIds: Map<Int, Long> = emptyMap()
) : TdLib {
    private val nextId = AtomicInteger()
    private val events = Channel<String>(Channel.UNLIMITED)
    private val accounts = ConcurrentHashMap<Int, Int>()
    private val started = ConcurrentHashMap.newKeySet<Int>()

    val requests = CopyOnWriteArrayList<String>()
    val instancesCreated get() = nextId.get()

    fun emitState(clientId: Int, state: String) = events.trySend(stateEvent(clientId, state))

    fun clientOf(account: Int): Int = accounts.entries.first { it.value == account }.key

    override fun createClientId(): Int = nextId.incrementAndGet()

    override fun send(clientId: Int, request: String) {
        val json = Json.parseToJsonElement(request).jsonObject
        val type = json.getValue("@type").jsonPrimitive.content
        val extra = json.getValue("@extra").jsonPrimitive.content
        requests += "$clientId $type"
        // TDLib starts an instance on its first request.
        if (started.add(clientId)) emitState(clientId, "authorizationStateWaitTdlibParameters")
        val answer = when (type) {
            "getMe" -> """"@type":"user","id":${userIdOf(clientId)},""" +
                """"status":{"@type":"userStatusEmpty"},"type":{"@type":"userTypeRegular"}"""

            else -> """"@type":"ok""""
        }
        events.trySend("""{$answer,"@extra":$extra,"@client_id":$clientId}""")
        when (type) {
            "setTdlibParameters" -> {
                val directory = json.getValue("database_directory").jsonPrimitive.content
                val account = directory.substringBeforeLast("/db").substringAfterLast('/').toInt()
                accounts[clientId] = account
                startStates[account].orEmpty()
                    .ifEmpty { listOf("authorizationStateWaitPhoneNumber") }
                    .forEach { emitState(clientId, it) }
            }

            "logOut" -> listOf(
                "authorizationStateLoggingOut",
                "authorizationStateClosing",
                "authorizationStateClosed"
            ).forEach { emitState(clientId, it) }

            "close" -> listOf("authorizationStateClosing", "authorizationStateClosed")
                .forEach { emitState(clientId, it) }
        }
    }

    override suspend fun receive(timeout: Duration): String? = withTimeoutOrNull(timeout) {
        events.receive()
    }

    override suspend fun execute(request: String): String? = null

    override fun setLogMessageHandler(maxVerbosityLevel: Int, handler: TdLogMessageHandler?) = Unit

    private fun userIdOf(clientId: Int): Long {
        val account = accounts.getValue(clientId)
        return userIds[account] ?: (USER_ID_BASE + account)
    }

    private fun stateEvent(clientId: Int, state: String) =
        """{"@type":"updateAuthorizationState","authorization_state":{"@type":"$state"},"@client_id":$clientId}"""

    private companion object {
        const val USER_ID_BASE = 1000L
    }
}
