package com.xbot.kotogram.feature.language.data.repository

import arrow.core.Either
import arrow.core.raise.either
import co.touchlab.kermit.Logger
import com.xbot.kotogram.core.account.AppClient
import com.xbot.kotogram.feature.language.data.BUNDLED_LANGUAGE_PACK_ID
import com.xbot.kotogram.feature.language.data.extensions.isFor
import com.xbot.kotogram.feature.language.data.extensions.languagePack
import com.xbot.kotogram.feature.language.data.extensions.toLanguageError
import com.xbot.kotogram.feature.language.data.extensions.withChanges
import com.xbot.kotogram.feature.language.data.platform.PlatformLanguage
import com.xbot.kotogram.feature.language.data.store.ChosenLanguageStore
import com.xbot.kotogram.feature.language.domain.model.LanguageError
import com.xbot.kotogram.feature.language.domain.model.LanguagePack
import com.xbot.kotogram.feature.language.domain.model.LanguageState
import com.xbot.kotogram.feature.language.domain.repository.LanguageRepository
import io.xbot.tdlib.api.Error
import io.xbot.tdlib.api.GetLanguagePackInfo
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.UpdateLanguagePackStrings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

internal class DefaultLanguageRepository(
    private val appScope: CoroutineScope,
    private val client: AppClient,
    private val store: ChosenLanguageStore,
    private val platform: PlatformLanguage? = null,
) : LanguageRepository {
    private val logger = Logger.withTag("LanguageRepository")
    private val mutex = Mutex()
    private val restored = CompletableDeferred<Unit>()

    private val chosen = MutableStateFlow<LanguagePackInfo?>(null)

    private val mutableState = MutableStateFlow<LanguageState>(LanguageState.Loading)
    override val state: StateFlow<LanguageState> = mutableState.asStateFlow()

    val languagePackId: Flow<String> = flow {
        restored.await()
        emitAll(chosen.map { it?.id.orEmpty() })
    }.distinctUntilChanged()

    init {
        appScope.launch {
            mutex.withLock { restore() }
            platform?.languagePackId?.filterNotNull()?.collect(::follow)
        }
    }

    override suspend fun apply(languagePackId: String): Either<LanguageError, Unit> = appScope.async {
        mutex.withLock { switchTo(languagePackId) }.onRight { platform?.sync(chosenLanguagePackId()) }
    }.await()

    suspend fun onStringsChanged(update: UpdateLanguagePackStrings) {
        mutex.withLock {
            val info = chosen.value?.takeIf { update.isFor(it) } ?: return
            val loaded = (state.value as? LanguageState.Loaded)?.pack
            val next = if (loaded == null || update.strings.isEmpty()) {
                download(info).getOrNull()
            } else {
                loaded.withChanges(update)
            }
            if (next != null) mutableState.value = LanguageState.Loaded(next)
        }
    }

    private suspend fun restore() {
        val info = store.load()
        chosen.value = info
        restored.complete(Unit)
        if (info == null) {
            mutableState.value = LanguageState.Bundled
            return
        }
        val fallback = appScope.launch {
            delay(RESTORE_TIMEOUT)
            mutableState.compareAndSet(LanguageState.Loading, LanguageState.Bundled)
        }
        mutableState.value = download(info).fold(ifLeft = { LanguageState.Bundled }, ifRight = LanguageState::Loaded)
        fallback.cancel()
    }

    private suspend fun follow(languagePackId: String) {
        val followed = mutex.withLock { languagePackId == chosenLanguagePackId() || switchTo(languagePackId).isRight() }
        if (followed) platform?.sync(chosenLanguagePackId())
    }

    private fun chosenLanguagePackId(): String = chosen.value?.id ?: BUNDLED_LANGUAGE_PACK_ID

    private suspend fun switchTo(languagePackId: String): Either<LanguageError, Unit> = either {
        if (languagePackId == BUNDLED_LANGUAGE_PACK_ID) {
            choose(info = null)
            mutableState.value = LanguageState.Bundled
        } else {
            mutableState.value = LanguageState.Loaded(fetch(languagePackId).bind())
        }
    }.mapLeft { it.toLanguageError() }

    private suspend fun fetch(languagePackId: String): Either<Error, LanguagePack> = either {
        val info = client.send(GetLanguagePackInfo(languagePackId = languagePackId)).bind()
        download(info).bind().also { choose(info) }
    }

    private suspend fun choose(info: LanguagePackInfo?) {
        store.save(info)
        chosen.value = info
    }

    private suspend fun download(info: LanguagePackInfo): Either<Error, LanguagePack> {
        val (pack, time) = measureTimedValue { client.languagePack(info) }
        pack.fold(
            ifLeft = { logger.e { "Can't get language pack ${info.id}: ${it.message}" } },
            ifRight = { logger.d { "Language pack ${info.id}: ${it.strings.size} strings in $time" } },
        )
        return pack
    }

    private companion object {
        val RESTORE_TIMEOUT = 3.seconds
    }
}
