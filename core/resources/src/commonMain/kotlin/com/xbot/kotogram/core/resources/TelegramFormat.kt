package com.xbot.kotogram.core.resources

private val Placeholder = Regex("""%(?:(\d+)\$)?[@sd]|%%|\*\*""")

// macOS's format: %1$@ and %1$d by position, bare %@ and %d in order; **bold** marks are dropped.
internal fun String.formatTelegram(args: Array<out Any>): String {
    var next = 0
    return Placeholder.replace(this) { match ->
        when (match.value) {
            "%%" -> "%"

            "**" -> ""

            else -> {
                val index = match.groupValues[1].toIntOrNull()?.minus(1) ?: next++
                args.getOrNull(index)?.toString() ?: match.value
            }
        }
    }
}
