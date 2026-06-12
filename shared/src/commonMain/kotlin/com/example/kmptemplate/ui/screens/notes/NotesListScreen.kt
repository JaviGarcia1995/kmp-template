package com.example.kmptemplate.ui.screens.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kmptemplate.presentation.notes.NoteListItemUi
import com.example.kmptemplate.presentation.notes.NotesError
import com.example.kmptemplate.presentation.notes.NotesUiState
import com.example.kmptemplate.presentation.notes.NotesViewModel
import com.example.kmptemplate.ui.theme.Dimensions
import com.example.kmptemplate.ui.theme.TypeScale
import kmptemplate.shared.generated.resources.Res
import kmptemplate.shared.generated.resources.notes_add
import kmptemplate.shared.generated.resources.notes_empty_body
import kmptemplate.shared.generated.resources.notes_empty_title
import kmptemplate.shared.generated.resources.notes_error_load
import kmptemplate.shared.generated.resources.notes_list_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onAddNote: () -> Unit,
    onNoteSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    NotesListContent(
        state = state,
        actions = NotesListActions(
            onAddNote = onAddNote,
            onNoteSelected = onNoteSelected
        ),
        modifier = modifier
    )
}

data class NotesListActions(
    val onAddNote: () -> Unit,
    val onNoteSelected: (Long) -> Unit
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NotesListContent(
    state: NotesUiState,
    actions: NotesListActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.notes_list_title),
                        style = TypeScale.Headline2
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = actions.onAddNote,
                content = {
                    Text(
                        text = stringResource(Res.string.notes_add),
                        style = TypeScale.Label1
                    )
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingNotes(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            state.error != null -> NotesMessage(
                title = stringResource(Res.string.notes_error_load),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            state.notes.isEmpty() -> NotesMessage(
                title = stringResource(Res.string.notes_empty_title),
                body = stringResource(Res.string.notes_empty_body),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            else -> NotesList(
                notes = state.notes,
                onNoteSelected = actions.onNoteSelected,
                contentPadding = innerPadding
            )
        }
    }
}

@Composable
private fun NotesList(
    notes: List<NoteListItemUi>,
    onNoteSelected: (Long) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimensions.ScreenHorizontalPadding,
            top = contentPadding.calculateTopPadding() + Dimensions.ScreenContentSpacing,
            end = Dimensions.ScreenHorizontalPadding,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Dimensions.ScreenContentSpacing)
    ) {
        items(notes, key = NoteListItemUi::id) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNoteSelected(note.id) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = note.title,
                        style = TypeScale.Title2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = TypeScale.Body2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingNotes(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NotesMessage(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null
) {
    Column(
        modifier = modifier.padding(Dimensions.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = TypeScale.Title2)
        if (body != null) {
            Text(
                text = body,
                modifier = Modifier.padding(top = 8.dp),
                style = TypeScale.Body2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
