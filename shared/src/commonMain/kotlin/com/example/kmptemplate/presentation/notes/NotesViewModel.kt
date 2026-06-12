package com.example.kmptemplate.presentation.notes

import com.example.kmptemplate.dispatcher.DispatcherProvider
import com.example.kmptemplate.domain.model.Note
import com.example.kmptemplate.domain.repository.NotesRepository
import com.example.kmptemplate.presentation.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update

class NotesViewModel(
    dispatchers: DispatcherProvider,
    private val notesRepository: NotesRepository
) : BaseViewModel(dispatchers) {

    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<NotesUiEffect>(extraBufferCapacity = 1)
    val effects = _effects

    private var editingNote: Note? = null

    init {
        observeNotes()
    }

    fun onEditorOpened(noteId: Long?) {
        editingNote = null
        _state.update { current ->
            current.copy(
                editor = NoteEditorUiState(
                    noteId = noteId,
                    isLoading = noteId != null
                )
            )
        }
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    fun onTitleChanged(title: String) {
        _state.update { current ->
            current.copy(editor = current.editor.copy(title = title, error = null))
        }
    }

    fun onContentChanged(content: String) {
        _state.update { current ->
            current.copy(editor = current.editor.copy(content = content, error = null))
        }
    }

    fun onSaveRequested() {
        val editor = _state.value.editor
        if (!editor.canSave) return

        _state.update { current ->
            current.copy(editor = current.editor.copy(isSaving = true, error = null))
        }
        launch(dispatchers.io) {
            try {
                val title = editor.title.trim()
                val content = editor.content.trim()
                val noteId = editor.noteId
                if (noteId == null) {
                    val savedNoteId = notesRepository.createNote(title, content)
                    _effects.emit(NotesUiEffect.Saved(savedNoteId))
                } else {
                    val note = editingNote
                    if (note == null || note.id != noteId) {
                        setEditorError(NotesError.NOT_FOUND)
                        return@launch
                    }
                    notesRepository.updateNote(note.copy(title = title, content = content))
                    _effects.emit(NotesUiEffect.Saved(noteId))
                }
            } catch (_: Throwable) {
                setEditorError(NotesError.SAVE_FAILED)
            } finally {
                setSaving(false)
            }
        }
    }

    fun onDeleteRequested() {
        val noteId = _state.value.editor.noteId ?: return
        if (_state.value.editor.isSaving) return

        setSaving(true)
        launch(dispatchers.io) {
            runCatching {
                notesRepository.deleteNote(noteId)
            }.onSuccess {
                _effects.emit(NotesUiEffect.Deleted)
            }.onFailure {
                setEditorError(NotesError.DELETE_FAILED)
            }
            setSaving(false)
        }
    }

    private fun observeNotes() {
        launch(dispatchers.io) {
            runCatching {
                notesRepository.observeNotes().collect { notes ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            notes = notes.map(Note::toListItem),
                            error = null
                        )
                    }
                }
            }.onFailure {
                _state.update { current ->
                    current.copy(isLoading = false, error = NotesError.LOAD_FAILED)
                }
            }
        }
    }

    private fun loadNote(noteId: Long) {
        launch(dispatchers.io) {
            runCatching {
                notesRepository.observeNote(noteId).firstOrNull()
            }.onSuccess { note ->
                if (_state.value.editor.noteId != noteId) return@onSuccess
                editingNote = note
                _state.update { current ->
                    current.copy(
                        editor = if (note == null) {
                            current.editor.copy(
                                isLoading = false,
                                error = NotesError.NOT_FOUND
                            )
                        } else {
                            NoteEditorUiState(
                                noteId = note.id,
                                title = note.title,
                                content = note.content
                            )
                        }
                    )
                }
            }.onFailure {
                if (_state.value.editor.noteId != noteId) return@onFailure
                setEditorError(NotesError.LOAD_FAILED)
            }
        }
    }

    private fun setSaving(isSaving: Boolean) {
        _state.update { current ->
            current.copy(editor = current.editor.copy(isSaving = isSaving))
        }
    }

    private fun setEditorError(error: NotesError) {
        _state.update { current ->
            current.copy(
                editor = current.editor.copy(
                    isLoading = false,
                    isSaving = false,
                    error = error
                )
            )
        }
    }
}

data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: List<NoteListItemUi> = emptyList(),
    val error: NotesError? = null,
    val editor: NoteEditorUiState = NoteEditorUiState()
)

data class NoteListItemUi(
    val id: Long,
    val title: String,
    val content: String
)

data class NoteEditorUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: NotesError? = null
) {
    val canSave: Boolean
        get() = title.isNotBlank() && !isLoading && !isSaving
}

enum class NotesError {
    LOAD_FAILED,
    SAVE_FAILED,
    DELETE_FAILED,
    NOT_FOUND
}

sealed interface NotesUiEffect {
    data class Saved(val noteId: Long) : NotesUiEffect

    data object Deleted : NotesUiEffect
}

private fun Note.toListItem() = NoteListItemUi(
    id = id,
    title = title,
    content = content
)
