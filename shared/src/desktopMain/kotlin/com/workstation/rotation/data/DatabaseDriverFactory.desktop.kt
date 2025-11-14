package com.workstation.rotation.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.workstation.rotation.database.AppDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".workstation-rotation")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "workstation_rotation.db")
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        
        // Solo crear el esquema si la base de datos no existe
        if (!databaseFile.exists() || databaseFile.length() == 0L) {
            AppDatabase.Schema.create(driver)
        }
        
        return driver
    }
}
