package com.xbot.kotogram.feature.chats.impl.component

internal fun interface RowMapper<E, R> {
    fun map(entries: List<E>): List<R>
}

// Reuses a row while its inputs are the same, so strong skipping's === skips it. One collector at a time.
internal class MemoizingRowMapper<E, K, R>(
    private val key: (E) -> K,
    private val hasSameInputs: (old: E, new: E) -> Boolean,
    private val toRow: (E) -> R
) : RowMapper<E, R> {
    private class Memo<E, R>(val entry: E, val row: R)

    private var memos = emptyMap<K, Memo<E, R>>()

    override fun map(entries: List<E>): List<R> {
        val next = HashMap<K, Memo<E, R>>(entries.size)
        val rows = entries.map { entry ->
            val id = key(entry)
            val memo = memos[id]?.takeIf { hasSameInputs(it.entry, entry) }
                ?: Memo(entry, toRow(entry))
            next[id] = memo
            memo.row
        }
        memos = next
        return rows
    }
}
