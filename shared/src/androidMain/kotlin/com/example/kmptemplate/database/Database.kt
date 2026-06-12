package com.example.kmptemplate.database

import app.cash.sqldelight.db.SqlDriver

actual class Database(actual val queries: AppDatabaseQueries)

actual fun createDatabase(driver: SqlDriver): Database {
    val database = AppDatabase(driver)
    return Database(database.appDatabaseQueries)
}
