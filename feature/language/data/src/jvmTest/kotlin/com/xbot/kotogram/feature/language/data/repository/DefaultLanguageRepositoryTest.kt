package com.xbot.kotogram.feature.language.data.repository

import com.xbot.kotogram.feature.language.data.FakeAppClient
import com.xbot.kotogram.feature.language.data.FakePlatformLanguage
import com.xbot.kotogram.feature.language.data.store.ChosenLanguageStore
import com.xbot.kotogram.feature.language.domain.model.LanguageError
import com.xbot.kotogram.feature.language.domain.model.LanguagePack
import com.xbot.kotogram.feature.language.domain.model.LanguageState
import io.xbot.tdlib.api.GetLanguagePackInfo
import io.xbot.tdlib.api.GetLanguagePackStrings
import io.xbot.tdlib.api.LanguagePackInfo
import io.xbot.tdlib.api.LanguagePackString
import io.xbot.tdlib.api.LanguagePackStringValueDeleted
import io.xbot.tdlib.api.LanguagePackStringValueOrdinary
import io.xbot.tdlib.api.UpdateLanguagePackStrings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class DefaultLanguageRepositoryTest {
    private val russian = LanguagePackInfo(id = "ru", pluralCode = "ru")
    private val custom = LanguagePackInfo(id = "x-ru", baseLanguagePackId = "ru", pluralCode = "ru")
    private val client = FakeAppClient(
        infos = mapOf("ru" to russian, "x-ru" to custom),
        packs = mapOf(
            "ru" to [ordinary("A", "а"), ordinary("B", "б"), deleted("C")],
            "x-ru" to [ordinary("A", "своё а")],
        ),
    )
    private val store = InMemoryChosenLanguageStore()

    private fun TestScope.repository(platform: FakePlatformLanguage? = null) =
        DefaultLanguageRepository(backgroundScope, client, store, platform)

    @Test
    fun withoutAChoiceTheBundledStringsShowAtOnce() = runTest {
        val repository = repository()
        runCurrent()
        assertEquals(LanguageState.Bundled, repository.state.value)
        assertEquals("", repository.languagePackId.first())
        assertEquals([], client.requests)
    }

    @Test
    fun theChosenPackComesBackFromTdlib() = runTest {
        store.saved = russian
        val repository = repository()
        runCurrent()
        assertEquals(mapOf("A" to "а", "B" to "б"), loaded(repository).texts())
        assertEquals("ru", repository.languagePackId.first())
    }

    @Test
    fun aSlowTdlibShowsEnglishMeanwhile() = runTest {
        store.saved = russian
        client.started = CompletableDeferred()
        val repository = repository()
        runCurrent()
        assertEquals(LanguageState.Loading, repository.state.value)
        assertEquals("ru", repository.languagePackId.first())

        advanceTimeBy(4.seconds)
        assertEquals(LanguageState.Bundled, repository.state.value)

        client.started.complete(Unit)
        runCurrent()
        assertEquals("а", loaded(repository).texts()["A"])
    }

    @Test
    fun aSwitchStoresThePackAndShowsIt() = runTest {
        val repository = repository()

        assertEquals(Unit, repository.apply("ru").getOrNull())

        assertEquals(russian, store.saved)
        assertEquals("б", loaded(repository).texts()["B"])
        assertEquals("ru", repository.languagePackId.first())
    }

    @Test
    fun aCustomPackFallsBackToItsBase() = runTest {
        val repository = repository()
        repository.apply("x-ru")
        val pack = loaded(repository)
        assertEquals("своё а", (pack.string("A") as LanguagePackStringValueOrdinary).value)
        assertEquals("б", (pack.string("B") as LanguagePackStringValueOrdinary).value)
    }

    @Test
    fun aFailedSwitchKeepsTheLanguage() = runTest {
        val repository = repository()
        runCurrent()

        val result = repository.apply("de")

        assertIs<LanguageError.Unknown>(result.leftOrNull())
        assertEquals(LanguageState.Bundled, repository.state.value)
        assertNull(store.saved)
    }

    @Test
    fun englishClearsTheChoice() = runTest {
        store.saved = russian
        val repository = repository()

        repository.apply("en")

        assertEquals(LanguageState.Bundled, repository.state.value)
        assertNull(store.saved)
        assertEquals("", repository.languagePackId.first())
    }

    @Test
    fun anUpdateChangesOnlyItsStrings() = runTest {
        store.saved = russian
        val repository = repository()
        runCurrent()
        val requests = client.requests.size

        repository.onStringsChanged(
            update("ru", ordinary("A", "новое а"), deleted("B")),
        )

        assertEquals(mapOf("A" to "новое а"), loaded(repository).texts())
        assertEquals(requests, client.requests.size)
    }

    @Test
    fun anUpdateWithoutStringsReadsThePackAgain() = runTest {
        store.saved = russian
        val repository = repository()
        runCurrent()
        client.packs = client.packs + ("ru" to [ordinary("A", "а2")])

        repository.onStringsChanged(update("ru"))

        assertEquals(mapOf("A" to "а2"), loaded(repository).texts())
        assertEquals(2, client.requests.count { it is GetLanguagePackStrings })
    }

    @Test
    fun anUpdateOfAnotherPackOrTargetChangesNothing() = runTest {
        store.saved = russian
        val repository = repository()
        runCurrent()
        val before = repository.state.value

        repository.onStringsChanged(update("de", ordinary("A", "x")))
        repository.onStringsChanged(update("ru", ordinary("A", "x")).copy(localizationTarget = "android"))

        assertEquals(before, repository.state.value)
    }

    @Test
    fun withoutAChoiceInTheSystemTheAppsOwnChoiceStands() = runTest {
        store.saved = russian
        val repository = repository(FakePlatformLanguage())
        runCurrent()

        assertEquals("а", loaded(repository).texts()["A"])
    }

    @Test
    fun aLanguageChosenInTheSystemLoadsBehindTheBundledOne() = runTest {
        client.started = CompletableDeferred()
        val repository = repository(FakePlatformLanguage("ru"))
        runCurrent()
        assertEquals(LanguageState.Bundled, repository.state.value)

        client.started.complete(Unit)
        runCurrent()
        assertEquals("а", loaded(repository).texts()["A"])
        assertEquals(russian, store.saved)
        assertEquals("ru", repository.languagePackId.first())
    }

    @Test
    fun theSystemsChoiceThatIsAlreadyStoredIsNotFetchedAgain() = runTest {
        store.saved = russian
        val platform = FakePlatformLanguage("ru")
        val repository = repository(platform)
        runCurrent()

        assertEquals("а", loaded(repository).texts()["A"])
        assertEquals(0, client.requests.count { it is GetLanguagePackInfo })
        assertEquals(["ru"], platform.synced)
    }

    @Test
    fun aFollowedChoiceIsSyncedBack() = runTest {
        val platform = FakePlatformLanguage("ru")
        val repository = repository(platform)
        runCurrent()

        assertEquals("а", loaded(repository).texts()["A"])
        assertEquals(["ru"], platform.synced)
    }

    @Test
    fun aChangeInTheSystemSwitchesThePack() = runTest {
        val platform = FakePlatformLanguage()
        val repository = repository(platform)
        runCurrent()
        assertEquals(LanguageState.Bundled, repository.state.value)

        platform.languagePackId.value = "ru"
        runCurrent()
        assertEquals("б", loaded(repository).texts()["B"])
        assertEquals(russian, store.saved)

        platform.languagePackId.value = "en"
        runCurrent()
        assertEquals(LanguageState.Bundled, repository.state.value)
        assertNull(store.saved)
    }

    @Test
    fun aChoiceTakenBackInTheSystemKeepsTheLanguage() = runTest {
        val platform = FakePlatformLanguage("ru")
        val repository = repository(platform)
        runCurrent()

        platform.languagePackId.value = null
        runCurrent()

        assertEquals("а", loaded(repository).texts()["A"])
    }

    @Test
    fun aLanguageOfTheSystemWithoutAPackLeavesTheLanguageAndIsNotSynced() = runTest {
        val platform = FakePlatformLanguage("de")
        val repository = repository(platform)
        runCurrent()

        assertEquals(LanguageState.Bundled, repository.state.value)
        assertNull(store.saved)
        assertEquals([], platform.synced)
    }

    @Test
    fun aSwitchInTheAppIsToldToTheSystem() = runTest {
        val platform = FakePlatformLanguage()
        val repository = repository(platform)
        runCurrent()

        assertEquals(Unit, repository.apply("ru").getOrNull())

        assertEquals("б", loaded(repository).texts()["B"])
        assertEquals(["ru"], platform.synced)
    }

    @Test
    fun aFailedSwitchIsNotToldToTheSystem() = runTest {
        val platform = FakePlatformLanguage()
        val repository = repository(platform)
        runCurrent()

        assertIs<LanguageError.Unknown>(repository.apply("de").leftOrNull())

        assertEquals([], platform.synced)
    }

    private fun loaded(repository: DefaultLanguageRepository): LanguagePack =
        assertIs<LanguageState.Loaded>(repository.state.value).pack

    private fun LanguagePack.texts() = strings.mapValues { (it.value as LanguagePackStringValueOrdinary).value }

    private fun ordinary(
        key: String,
        value: String,
    ) = LanguagePackString(key, LanguagePackStringValueOrdinary(value))

    private fun deleted(key: String) = LanguagePackString(key, LanguagePackStringValueDeleted)

    private fun update(
        languagePackId: String,
        vararg strings: LanguagePackString,
    ) = UpdateLanguagePackStrings(
        localizationTarget = "macos",
        languagePackId = languagePackId,
        strings = strings.toList(),
    )
}

private class InMemoryChosenLanguageStore : ChosenLanguageStore {
    var saved: LanguagePackInfo? = null

    override suspend fun load(): LanguagePackInfo? = saved

    override suspend fun save(info: LanguagePackInfo?) {
        saved = info
    }
}
