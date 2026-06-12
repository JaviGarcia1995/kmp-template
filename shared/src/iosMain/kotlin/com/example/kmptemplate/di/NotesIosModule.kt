package com.example.kmptemplate.di

import app.cash.sqldelight.db.SqlDriver
import com.example.kmptemplate.database.AppDatabaseQueries
import com.example.kmptemplate.database.Database
import com.example.kmptemplate.database.DatabaseDriverFactory
import com.example.kmptemplate.database.createDatabase
import org.koin.dsl.module

val notesIosModule = module {
    single { DatabaseDriverFactory() }
    single<SqlDriver> { get<DatabaseDriverFactory>().createDriver() }
    single<Database> { createDatabase(get()) }
    single<AppDatabaseQueries> { get<Database>().queries }
}
