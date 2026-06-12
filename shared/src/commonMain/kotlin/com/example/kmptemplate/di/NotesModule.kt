package com.example.kmptemplate.di

import com.example.kmptemplate.data.datasource.NotesLocalDataSource
import com.example.kmptemplate.data.datasource.sqldelight.SqlDelightNotesDataSource
import com.example.kmptemplate.data.repository.sqldelight.SqlDelightNotesRepositoryImpl
import com.example.kmptemplate.domain.repository.NotesRepository
import com.example.kmptemplate.dispatcher.DefaultDispatcherProvider
import com.example.kmptemplate.dispatcher.DispatcherProvider
import com.example.kmptemplate.presentation.notes.NotesViewModel
import com.example.kmptemplate.time.DefaultTimeProvider
import com.example.kmptemplate.time.TimeProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notesModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<TimeProvider> { DefaultTimeProvider() }
    single<NotesLocalDataSource> { SqlDelightNotesDataSource(get(), get()) }
    single { SqlDelightNotesRepositoryImpl(get(), get()) } bind NotesRepository::class
    factoryOf(::NotesViewModel)
}
