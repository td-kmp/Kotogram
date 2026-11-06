package com.xbot.kotogram.core.navigation.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.dropUnlessResumed(block: () -> Unit): () -> Unit = {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) block()
}
