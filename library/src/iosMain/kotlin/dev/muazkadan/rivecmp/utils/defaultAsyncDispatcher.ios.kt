package dev.muazkadan.rivecmp.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual val defaultAsyncDispatcher: CoroutineDispatcher = Dispatchers.IO