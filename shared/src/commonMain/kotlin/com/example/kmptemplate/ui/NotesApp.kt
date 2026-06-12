package com.example.kmptemplate.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.kmptemplate.navigation.NotesNavigation
import com.example.kmptemplate.presentation.notes.NotesViewModel
import com.example.kmptemplate.ui.theme.AppTheme

@Composable
fun NotesApp(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier
) {
    AppTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NotesNavigation(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
