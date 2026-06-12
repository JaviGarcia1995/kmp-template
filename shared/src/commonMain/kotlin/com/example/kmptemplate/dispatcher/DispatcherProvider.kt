package com.example.kmptemplate.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

internal expect val platformMainDispatcher: CoroutineDispatcher
internal expect val platformIODispatcher: CoroutineDispatcher
internal expect val platformDefaultDispatcher: CoroutineDispatcher

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = platformMainDispatcher
    override val io: CoroutineDispatcher = platformIODispatcher
    override val default: CoroutineDispatcher = platformDefaultDispatcher
}

