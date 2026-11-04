package com.xbot.kotogram.feature.chats.impl.component

/** Display rows of a list, built off the main thread. */
internal fun interface RowMapper<E, R> {
    fun map(entries: List<E>): List<R>
}

/**
 * Reuses a row while its entry has the same inputs, so strong skipping's `===` skips unchanged rows.
 * Not thread-safe: one collector at a time.
 */
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
        // Dropping what left the list also forgets a previous login: its snapshot was emptied.
        memos = next
        return rows
    }
}
