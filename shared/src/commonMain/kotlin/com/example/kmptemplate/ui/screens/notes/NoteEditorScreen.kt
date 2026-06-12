package com.example.kmptemplate.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmptemplate.presentation.notes.NoteEditorUiState
import com.example.kmptemplate.presentation.notes.NotesError
import com.example.kmptemplate.presentation.notes.NotesUiEffect
import com.example.kmptemplate.presentation.notes.NotesViewModel
import com.example.kmptemplate.ui.theme.Dimensions
import com.example.kmptemplate.ui.theme.TypeScale
import kmptemplate.shared.generated.resources.Res
import kmptemplate.shared.generated.resources.common_back
import kmptemplate.shared.generated.resources.common_save
import kmptemplate.shared.generated.resources.notes_content_label
import kmptemplate.shared.generated.resources.notes_delete
import kmptemplate.shared.generated.resources.notes_editor_edit_title
import kmptemplate.shared.generated.resources.notes_editor_new_title
import kmptemplate.shared.generated.resources.notes_error_delete
import kmptemplate.shared.generated.resources.notes_error_load
import kmptemplate.shared.generated.resources.notes_error_not_found
import kmptemplate.shared.generated.resources.notes_error_save
import kmptemplate.shared.generated.resources.notes_title_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    noteId: Long?,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(noteId) {
        viewModel.onEditorOpened(noteId)
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NotesUiEffect.Saved -> onFinished()
                NotesUiEffect.Deleted -> onFinished()
            }
        }
    }

    NoteEditorContent(
        state = state.editor,
        actions = NoteEditorActions(
            onBack = onBack,
            onTitleChanged = viewModel::onTitleChanged,
            onContentChanged = viewModel::onContentChanged,
            onSave = viewModel::onSaveRequested,
            onDelete = viewModel::onDeleteRequested
        ),
        modifier = modifier
    )
}

data class NoteEditorActions(
    val onBack: () -> Unit,
    val onTitleChanged: (String) -> Unit,
    val onContentChanged: (String) -> Unit,
    val onSave: () -> Unit,
    val onDelete: () -> Unit
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NoteEditorContent(
    state: NoteEditorUiState,
    actions: NoteEditorActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.noteId == null) {
                                Res.string.notes_editor_new_title
                            } else {
                                Res.string.notes_editor_edit_title
                            }
                        ),
                        style = TypeScale.Headline2
                    )
                },
                navigationIcon = {
                    TextButton(onClick = actions.onBack) {
                        Text(
                            text = stringResource(Res.string.common_back),
                            style = TypeScale.Label1
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            EditorBody(
                state = state,
                actions = actions,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun EditorBody(
    state: NoteEditorUiState,
    actions: NoteEditorActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = Dimensions.ScreenHorizontalPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimensions.ScreenContentSpacing)
    ) {
        state.error?.let { error ->
            Text(
                text = errorMessage(error),
                style = TypeScale.Body2,
                color = MaterialTheme.colorScheme.error
            )
        }
        OutlinedTextField(
            value = state.title,
            onValueChange = actions.onTitleChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = stringResource(Res.string.notes_title_label),
                    style = TypeScale.Label1
                )
            },
            textStyle = TypeScale.Body1,
            singleLine = true,
            enabled = !state.isSaving
        )
        OutlinedTextField(
            value = state.content,
            onValueChange = actions.onContentChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = stringResource(Res.string.notes_content_label),
                    style = TypeScale.Label1
                )
            },
            textStyle = TypeScale.Body1,
            minLines = 8,
            enabled = !state.isSaving
        )
        EditorButtons(state = state, actions = actions)
    }
}

@Composable
private fun EditorButtons(
    state: NoteEditorUiState,
    actions: NoteEditorActions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimensions.ScreenVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.noteId != null) {
            OutlinedButton(
                onClick = actions.onDelete,
                enabled = !state.isSaving
            ) {
                Text(
                    text = stringResource(Res.string.notes_delete),
                    style = TypeScale.Label1
                )
            }
        }
        Button(
            onClick = actions.onSave,
            enabled = state.canSave
        ) {
            Text(
                text = stringResource(Res.string.common_save),
                style = TypeScale.Label1
            )
        }
    }
}

@Composable
private fun errorMessage(error: NotesError): String =
    stringResource(
        when (error) {
            NotesError.LOAD_FAILED -> Res.string.notes_error_load
            NotesError.SAVE_FAILED -> Res.string.notes_error_save
            NotesError.DELETE_FAILED -> Res.string.notes_error_delete
            NotesError.NOT_FOUND -> Res.string.notes_error_not_found
        }
    )
