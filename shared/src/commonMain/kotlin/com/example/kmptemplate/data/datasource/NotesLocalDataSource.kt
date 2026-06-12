package com.example.kmptemplate.data.datasource

import kotlinx.coroutines.flow.Flow

data class NoteLocal(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

interface NotesLocalDataSource {
    fun observeNotes(): Flow<List<NoteLocal>>

    fun observeNote(id: Long): Flow<NoteLocal?>

    suspend fun insertNote(
        title: String,
        content: String,
        createdAt: Long,
        updatedAt: Long
    ): Long

    suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        updatedAt: Long
    )

    suspend fun deleteNote(id: Long)
}
