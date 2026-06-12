package com.example.kmptemplate.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.kmptemplate.presentation.notes.NotesViewModel
import com.example.kmptemplate.ui.screens.notes.NoteEditorScreen
import com.example.kmptemplate.ui.screens.notes.NotesListScreen
import kotlinx.serialization.Serializable

@Serializable
data object NotesList

@Serializable
data class NoteEditor(val noteId: Long? = null)

@Composable
fun NotesNavigation(
    viewModel: NotesViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NotesList,
        modifier = modifier
    ) {
        composable<NotesList> {
            NotesListScreen(
                viewModel = viewModel,
                onAddNote = { navController.navigate(NoteEditor()) },
                onNoteSelected = { noteId ->
                    navController.navigate(NoteEditor(noteId))
                }
            )
        }
        composable<NoteEditor> { backStackEntry ->
            val destination = backStackEntry.toRoute<NoteEditor>()
            NoteEditorScreen(
                viewModel = viewModel,
                noteId = destination.noteId,
                onBack = navController::popBackStack,
                onFinished = navController::popBackStack
            )
        }
    }
}
