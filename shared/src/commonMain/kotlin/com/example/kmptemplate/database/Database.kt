package com.example.kmptemplate.database

import app.cash.sqldelight.db.SqlDriver

expect class Database {
    val queries: AppDatabaseQueries
}

expect fun createDatabase(driver: SqlDriver): Database
