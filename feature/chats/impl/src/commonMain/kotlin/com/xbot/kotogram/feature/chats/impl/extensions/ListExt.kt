package com.xbot.kotogram.feature.chats.impl.extensions

internal inline fun <T> List<T>.hasSameItemsAs(
    other: List<T>,
    selector: (T) -> Any? = { it },
): Boolean {
    var same = size == other.size
    var index = 0
    while (same && index < size) {
        same = selector(this[index]) === selector(other[index])
        index++
    }
    return same
}

internal inline fun <T> List<T>.hasEqualItemsAs(
    other: List<T>,
    selector: (T) -> Any?,
): Boolean {
    var same = size == other.size
    var index = 0
    while (same && index < size) {
        same = selector(this[index]) == selector(other[index])
        index++
    }
    return same
}
