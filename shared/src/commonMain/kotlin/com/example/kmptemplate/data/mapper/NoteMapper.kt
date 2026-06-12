package com.example.kmptemplate.data.mapper

import com.example.kmptemplate.data.datasource.NoteLocal
import com.example.kmptemplate.database.LocalDateTimeAdapter
import com.example.kmptemplate.domain.model.Note

fun NoteLocal.toDomain(): Note =
    Note(
        id = id,
        title = title,
        content = content,
        createdAt = LocalDateTimeAdapter.decode(createdAt),
        updatedAt = LocalDateTimeAdapter.decode(updatedAt)
    )
