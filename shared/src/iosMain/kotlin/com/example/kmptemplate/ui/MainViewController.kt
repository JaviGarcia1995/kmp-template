package com.example.kmptemplate.ui

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.example.kmptemplate.di.notesIosModule
import com.example.kmptemplate.di.notesModule
import com.example.kmptemplate.presentation.notes.NotesViewModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController {
        val viewModel = remember {
            getKoin().get<NotesViewModel>()
        }
        NotesApp(viewModel = viewModel)
    }
}

private fun initKoin() {
    try {
        getKoin()
    } catch (_: Exception) {
        startKoin {
            modules(notesModule, notesIosModule)
        }
    }
}
