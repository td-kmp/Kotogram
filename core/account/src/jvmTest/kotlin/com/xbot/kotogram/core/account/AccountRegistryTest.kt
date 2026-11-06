package com.xbot.kotogram.core.account

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccountRegistryTest {
    private val one = AccountEntry(AccountId(1), userId = 10)
    private val two = AccountEntry(AccountId(2), userId = 20)
    private val signingIn = AccountEntry(AccountId(3))

    @Test
    fun idsNeverRepeatAfterARemoval() {
        val registry = AccountRegistry(listOf(one, two), active = one.id, lastId = 2)
        val next = registry.without(two.id).withNewAccount()
        assertEquals(AccountId(3), next.newestId)
        assertEquals(listOf(one.id, AccountId(3)), next.accounts.map { it.id })
    }

    @Test
    fun removingTheActiveAccountActivatesASignedInOne() {
        val registry = AccountRegistry(listOf(signingIn, one, two), active = two.id, lastId = 3)
        assertEquals(one.id, registry.without(two.id).active)
    }

    @Test
    fun removingAnotherAccountKeepsTheActiveOne() {
        val registry = AccountRegistry(listOf(one, two), active = two.id, lastId = 2)
        assertEquals(two.id, registry.without(one.id).active)
    }

    @Test
    fun aSignInInProgressNeverBecomesActiveOnItsOwn() {
        val registry = AccountRegistry(listOf(one, signingIn), active = one.id, lastId = 3)
        assertNull(registry.without(one.id).active)
    }

    @Test
    fun signedInLeavesOutASignInInProgress() {
        val registry = AccountRegistry(listOf(one, signingIn), active = one.id, lastId = 3)
        assertEquals(listOf(one), registry.signedIn)
    }
}
