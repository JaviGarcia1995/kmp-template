package com.example.kmptemplate.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.kmptemplate.android.di.notesAndroidModule
import com.example.kmptemplate.di.notesModule
import com.example.kmptemplate.presentation.notes.NotesViewModel
import com.example.kmptemplate.ui.NotesApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initKoin()
        enableEdgeToEdge()
        setContent {
            val viewModel = remember {
                GlobalContext.get().get<NotesViewModel>()
            }
            NotesApp(viewModel = viewModel)
        }
    }

    private fun initKoin() {
        try {
            getKoin()
        } catch (_: Exception) {
            startKoin {
                androidContext(applicationContext)
                modules(notesModule, notesAndroidModule)
            }
        }
    }
}
