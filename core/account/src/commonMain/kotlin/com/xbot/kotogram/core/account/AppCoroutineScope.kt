package com.xbot.kotogram.core.account

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named

val AppCoroutineScope: Qualifier = named("appCoroutineScope")
