package dev.muazkadan.rivecmp.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO