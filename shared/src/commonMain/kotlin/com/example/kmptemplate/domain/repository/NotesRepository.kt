package com.example.kmptemplate.domain.repository

import com.example.kmptemplate.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>

    fun observeNote(id: Long): Flow<Note?>

    suspend fun createNote(title: String, content: String): Long

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(id: Long)
}
