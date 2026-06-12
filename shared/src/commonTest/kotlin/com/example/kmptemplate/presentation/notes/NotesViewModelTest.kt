package com.example.kmptemplate.presentation.notes

import com.example.kmptemplate.dispatcher.DispatcherProvider
import com.example.kmptemplate.domain.model.Note
import com.example.kmptemplate.domain.repository.NotesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime

class NotesViewModelTest {

    @Test
    fun `observes notes and exposes list items`() = runBlocking {
        val repository = FakeNotesRepository(listOf(note(id = 1)))
        val viewModel = NotesViewModel(testDispatchers, repository)

        val state = viewModel.state.first { !it.isLoading }

        assertEquals(1L, state.notes.single().id)
        assertEquals("Note 1", state.notes.single().title)
    }

    @Test
    fun `new note requires a title and saves normalized fields`() = runBlocking {
        val repository = FakeNotesRepository()
        val viewModel = NotesViewModel(testDispatchers, repository)

        viewModel.onEditorOpened(noteId = null)
        viewModel.onTitleChanged("  New note  ")
        viewModel.onContentChanged("  Body  ")

        assertTrue(viewModel.state.value.editor.canSave)
        val savedEffect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onSaveRequested()
        savedEffect.await()

        assertEquals("New note" to "Body", repository.createdNote)
        assertFalse(viewModel.state.value.editor.isSaving)
    }

    @Test
    fun `opening existing note loads draft and update keeps its id`() = runBlocking {
        val repository = FakeNotesRepository(listOf(note(id = 4)))
        val viewModel = NotesViewModel(testDispatchers, repository)

        viewModel.onEditorOpened(noteId = 4)
        viewModel.state.first { !it.editor.isLoading && it.editor.noteId == 4L }
        viewModel.onTitleChanged("Updated")
        val savedEffect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onSaveRequested()
        savedEffect.await()

        assertEquals(4L, repository.updatedNote?.id)
        assertEquals("Updated", repository.updatedNote?.title)
    }

    @Test
    fun `late note load from a previous session does not overwrite the current editor`() = runBlocking {
        val repository = DeferredNotesRepository()
        val viewModel = NotesViewModel(testDispatchers, repository)

        viewModel.onEditorOpened(noteId = 1)
        viewModel.onEditorOpened(noteId = 2)
        repository.emit(1, note(id = 1))
        repository.emit(2, note(id = 2))

        viewModel.state.first { !it.editor.isLoading && it.editor.noteId == 2L }

        assertEquals(2L, viewModel.state.value.editor.noteId)
        assertEquals("Note 2", viewModel.state.value.editor.title)
    }

    @Test
    fun `saving existing note before it finishes loading does not crash`() = runBlocking {
        val repository = FakeNotesRepository(listOf(note(id = 8)))
        val viewModel = NotesViewModel(testDispatchers, repository)

        viewModel.onEditorOpened(noteId = 8)
        viewModel.onTitleChanged("Edited")
        viewModel.onContentChanged("Edited body")
        val savedEffect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onSaveRequested()

        savedEffect.await()

        assertEquals(8L, repository.updatedNote?.id)
        assertEquals("Edited", repository.updatedNote?.title)
    }

    @Test
    fun `blank title cannot be saved`() {
        val viewModel = NotesViewModel(testDispatchers, FakeNotesRepository())

        viewModel.onEditorOpened(noteId = null)
        viewModel.onContentChanged("Content")

        assertFalse(viewModel.state.value.editor.canSave)
    }

    private fun note(id: Long) = Note(
        id = id,
        title = "Note $id",
        content = "Content $id",
        createdAt = LocalDateTime(2026, 6, 10, 8, 0),
        updatedAt = LocalDateTime(2026, 6, 10, 9, 0)
    )

    private object testDispatchers : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }

    private class FakeNotesRepository(
        initialNotes: List<Note> = emptyList()
    ) : NotesRepository {
        private val notes = MutableStateFlow(initialNotes)

        var createdNote: Pair<String, String>? = null
        var updatedNote: Note? = null

        override fun observeNotes(): Flow<List<Note>> = notes

        override fun observeNote(id: Long): Flow<Note?> =
            MutableStateFlow(notes.value.firstOrNull { it.id == id })

        override suspend fun createNote(title: String, content: String): Long {
            createdNote = title to content
            return 10L
        }

        override suspend fun updateNote(note: Note) {
            updatedNote = note
        }

        override suspend fun deleteNote(id: Long) = Unit
    }

    private class DeferredNotesRepository : NotesRepository {
        private val noteFlows = mutableMapOf<Long, MutableSharedFlow<Note?>>()

        override fun observeNotes(): Flow<List<Note>> = MutableStateFlow(emptyList())

        override fun observeNote(id: Long): Flow<Note?> =
            noteFlows.getOrPut(id) { MutableSharedFlow(extraBufferCapacity = 1) }

        override suspend fun createNote(title: String, content: String): Long = 0L

        override suspend fun updateNote(note: Note) = Unit

        override suspend fun deleteNote(id: Long) = Unit

        suspend fun emit(id: Long, note: Note?) {
            noteFlows.getOrPut(id) { MutableSharedFlow(extraBufferCapacity = 1) }.emit(note)
        }
    }
}
