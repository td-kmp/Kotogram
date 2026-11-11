package com.xbot.kotogram.feature.chats.impl.extensions

internal fun <T> List<T>.hasSameItemsAs(other: List<T>): Boolean =
    size == other.size && indices.all { this[it] === other[it] }
