package com.xbot.kotogram.feature.language.data.extensions

import android.os.LocaleList
import java.util.Locale

internal fun LocaleList.toList(): List<Locale> = List(size()) { get(it) }
