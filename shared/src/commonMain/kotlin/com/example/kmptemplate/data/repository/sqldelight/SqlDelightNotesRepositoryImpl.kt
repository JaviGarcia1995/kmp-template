package com.example.kmptemplate.data.repository.sqldelight

import com.example.kmptemplate.data.datasource.NotesLocalDataSource
import com.example.kmptemplate.data.mapper.toDomain
import com.example.kmptemplate.database.LocalDateTimeAdapter
import com.example.kmptemplate.domain.model.Note
import com.example.kmptemplate.domain.repository.NotesRepository
import com.example.kmptemplate.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlDelightNotesRepositoryImpl(
    private val localDataSource: NotesLocalDataSource,
    private val timeProvider: TimeProvider
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        localDataSource.observeNotes().map { notes -> notes.map { it.toDomain() } }

    override fun observeNote(id: Long): Flow<Note?> =
        localDataSource.observeNote(id).map { it?.toDomain() }

    override suspend fun createNote(title: String, content: String): Long {
        val now = encodedNow()
        return localDataSource.insertNote(
            title = title,
            content = content,
            createdAt = now,
            updatedAt = now
        )
    }

    override suspend fun updateNote(note: Note) {
        localDataSource.updateNote(
            id = note.id,
            title = note.title,
            content = note.content,
            updatedAt = encodedNow()
        )
    }

    override suspend fun deleteNote(id: Long) {
        localDataSource.deleteNote(id)
    }

    private fun encodedNow(): Long =
        LocalDateTimeAdapter.encode(timeProvider.currentDateTime())
}
