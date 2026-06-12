package com.example.kmptemplate.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val platformMainDispatcher: CoroutineDispatcher = Dispatchers.Main
internal actual val platformIODispatcher: CoroutineDispatcher = Dispatchers.IO
internal actual val platformDefaultDispatcher: CoroutineDispatcher = Dispatchers.Default


