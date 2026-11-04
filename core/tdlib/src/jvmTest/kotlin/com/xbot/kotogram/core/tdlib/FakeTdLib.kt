package com.xbot.kotogram.core.tdlib

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

/** TDLib at its JSON seam. Each directory's accounts start in the scripted states; login by default. */
internal class FakeTdLib(private val startStates: Map<String, List<String>> = emptyMap()) : TdLib {
    private val nextId = AtomicInteger()
    private val events = Channel<String>(Channel.UNLIMITED)
    private val directories = ConcurrentHashMap<Int, String>()

    /** `"<client id> <@type>"` of every request, in order. */
    val requests = CopyOnWriteArrayList<String>()
    val instancesCreated get() = nextId.get()

    /** Requests that are never answered, as a TDLib that doesn't close. */
    var isStuck = false

    fun emitState(clientId: Int, state: String) = events.trySend(stateEvent(clientId, state))

    fun emit(clientId: Int, event: String) =
        events.trySend("""${event.dropLast(1)},"@client_id":$clientId}""")

    fun lastClientOf(directory: String): Int =
        directories.entries.filter { it.value.endsWith(directory) }.maxOf { it.key }

    override fun createClientId(): Int = nextId.incrementAndGet()

    override fun send(clientId: Int, request: String) {
        val json = Json.parseToJsonElement(request).jsonObject
        val type = json.getValue("@type").jsonPrimitive.content
        val extra = json.getValue("@extra").jsonPrimitive.content
        requests += "$clientId $type"
        if (isStuck) return
        events.trySend("""{"@type":"ok","@extra":$extra,"@client_id":$clientId}""")
        when (type) {
            "setTdlibParameters" -> {
                val directory = json.getValue("database_directory").jsonPrimitive.content
                directories[clientId] = directory
                val start = startStates.entries.firstOrNull { directory.endsWith(it.key) }?.value
                    ?: listOf("authorizationStateWaitPhoneNumber")
                start.forEach {
                    if (it.startsWith("{")) emit(clientId, it) else emitState(clientId, it)
                }
            }

            "logOut" -> listOf(
                "authorizationStateLoggingOut",
                "authorizationStateClosing",
                "authorizationStateClosed"
            ).forEach { emitState(clientId, it) }

            "destroy", "close" -> listOf("authorizationStateClosing", "authorizationStateClosed")
                .forEach { emitState(clientId, it) }
        }
    }

    override suspend fun receive(timeout: Duration): String? = withTimeoutOrNull(timeout) {
        events.receive()
    }

    override suspend fun execute(request: String): String? = null

    override fun setLogMessageHandler(maxVerbosityLevel: Int, handler: TdLogMessageHandler?) = Unit

    private fun stateEvent(clientId: Int, state: String) =
        """{"@type":"updateAuthorizationState","authorization_state":{"@type":"$state"},"@client_id":$clientId}"""
}
