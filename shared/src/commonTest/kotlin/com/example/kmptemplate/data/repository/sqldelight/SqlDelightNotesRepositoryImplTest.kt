package com.example.kmptemplate.data.repository.sqldelight

import com.example.kmptemplate.data.datasource.NoteLocal
import com.example.kmptemplate.data.datasource.NotesLocalDataSource
import com.example.kmptemplate.database.LocalDateTimeAdapter
import com.example.kmptemplate.domain.model.Note
import com.example.kmptemplate.time.TimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class SqlDelightNotesRepositoryImplTest {

    @Test
    fun `create note uses current time for creation and update timestamps`() = runBlocking {
        val now = LocalDateTime(2026, 6, 10, 9, 30)
        val dataSource = FakeNotesLocalDataSource()
        val repository = SqlDelightNotesRepositoryImpl(dataSource, FixedTimeProvider(now))

        val id = repository.createNote(title = "Title", content = "Content")

        assertEquals(1L, id)
        assertEquals(
            InsertCall(
                title = "Title",
                content = "Content",
                createdAt = LocalDateTimeAdapter.encode(now),
                updatedAt = LocalDateTimeAdapter.encode(now)
            ),
            dataSource.insertCall
        )
    }

    @Test
    fun `update note keeps content and uses current time as updated timestamp`() = runBlocking {
        val now = LocalDateTime(2026, 6, 10, 10, 45)
        val dataSource = FakeNotesLocalDataSource()
        val repository = SqlDelightNotesRepositoryImpl(dataSource, FixedTimeProvider(now))
        val note = note(id = 7)

        repository.updateNote(note.copy(title = "Updated", content = "New content"))

        assertEquals(
            UpdateCall(
                id = 7,
                title = "Updated",
                content = "New content",
                updatedAt = LocalDateTimeAdapter.encode(now)
            ),
            dataSource.updateCall
        )
    }

    @Test
    fun `observe notes maps local timestamps to domain`() = runBlocking {
        val expected = note(id = 3)
        val dataSource = FakeNotesLocalDataSource(
            initialNotes = listOf(expected.toLocal())
        )
        val repository = SqlDelightNotesRepositoryImpl(
            dataSource,
            FixedTimeProvider(expected.updatedAt)
        )

        assertEquals(listOf(expected), repository.observeNotes().first())
    }

    private fun note(id: Long) = Note(
        id = id,
        title = "Note $id",
        content = "Content $id",
        createdAt = LocalDateTime(2026, 6, 9, 8, 0),
        updatedAt = LocalDateTime(2026, 6, 10, 9, 0)
    )

    private fun Note.toLocal() = NoteLocal(
        id = id,
        title = title,
        content = content,
        createdAt = LocalDateTimeAdapter.encode(createdAt),
        updatedAt = LocalDateTimeAdapter.encode(updatedAt)
    )

    private class FixedTimeProvider(
        private val dateTime: LocalDateTime
    ) : TimeProvider {
        override fun currentDate(): LocalDate = dateTime.date

        override fun currentDateTime(): LocalDateTime = dateTime
    }

    private class FakeNotesLocalDataSource(
        initialNotes: List<NoteLocal> = emptyList()
    ) : NotesLocalDataSource {
        private val notes = MutableStateFlow(initialNotes)

        var insertCall: InsertCall? = null
        var updateCall: UpdateCall? = null

        override fun observeNotes(): Flow<List<NoteLocal>> = notes

        override fun observeNote(id: Long): Flow<NoteLocal?> =
            MutableStateFlow(notes.value.firstOrNull { it.id == id })

        override suspend fun insertNote(
            title: String,
            content: String,
            createdAt: Long,
            updatedAt: Long
        ): Long {
            insertCall = InsertCall(title, content, createdAt, updatedAt)
            return 1L
        }

        override suspend fun updateNote(
            id: Long,
            title: String,
            content: String,
            updatedAt: Long
        ) {
            updateCall = UpdateCall(id, title, content, updatedAt)
        }

        override suspend fun deleteNote(id: Long) = Unit
    }

    private data class InsertCall(
        val title: String,
        val content: String,
        val createdAt: Long,
        val updatedAt: Long
    )

    private data class UpdateCall(
        val id: Long,
        val title: String,
        val content: String,
        val updatedAt: Long
    )
}
