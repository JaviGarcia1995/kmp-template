package com.example.kmptemplate.data.datasource.sqldelight

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.kmptemplate.data.datasource.NoteLocal
import com.example.kmptemplate.data.datasource.NotesLocalDataSource
import com.example.kmptemplate.database.AppDatabaseQueries
import com.example.kmptemplate.dispatcher.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightNotesDataSource(
    private val queries: AppDatabaseQueries,
    private val dispatchers: DispatcherProvider
) : NotesLocalDataSource {

    override fun observeNotes(): Flow<List<NoteLocal>> =
        queries.selectAllNotes()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { notes -> notes.map { it.toLocal() } }

    override fun observeNote(id: Long): Flow<NoteLocal?> =
        queries.selectNoteById(id)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .map { it?.toLocal() }

    override suspend fun insertNote(
        title: String,
        content: String,
        createdAt: Long,
        updatedAt: Long
    ): Long = withContext(dispatchers.io) {
        queries.insertNote(title, content, createdAt, updatedAt)
        queries.getLastInsertRowId().executeAsOne()
    }

    override suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        updatedAt: Long
    ) = withContext(dispatchers.io) {
        queries.updateNote(title, content, updatedAt, id)
    }

    override suspend fun deleteNote(id: Long) = withContext(dispatchers.io) {
        queries.deleteNote(id)
    }

    private fun com.example.kmptemplate.database.Note.toLocal() =
        NoteLocal(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}
