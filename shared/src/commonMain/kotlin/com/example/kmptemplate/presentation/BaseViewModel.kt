package com.example.kmptemplate.presentation

import com.example.kmptemplate.dispatcher.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

open class BaseViewModel(
    protected val dispatchers: DispatcherProvider,
    coroutineScope: CoroutineScope? = null
) {
    private val ownsScope = coroutineScope == null
    private val supervisorJob = if (ownsScope) SupervisorJob() else null

    protected val scope: CoroutineScope = coroutineScope ?: CoroutineScope(
        requireNotNull(supervisorJob) + dispatchers.main
    )

    protected fun launch(
        dispatcher: CoroutineDispatcher = dispatchers.main,
        block: suspend CoroutineScope.() -> Unit
    ) = scope.launch(dispatcher, block = block)

    open fun clear() {
        if (ownsScope) {
            scope.cancel()
        }
    }
}

