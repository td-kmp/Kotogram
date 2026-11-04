package com.xbot.kotogram.core.navigation.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/** Wraps a navigation callback so a second tap during a transition doesn't navigate twice. */
fun LifecycleOwner.dropUnlessResumed(block: () -> Unit): () -> Unit = {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) block()
}
