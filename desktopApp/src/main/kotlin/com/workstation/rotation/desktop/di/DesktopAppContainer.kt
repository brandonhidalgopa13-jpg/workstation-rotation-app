package com.workstation.rotation.desktop.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.repository.RotationRepository
import com.workstation.rotation.domain.service.RotationService
import com.workstation.rotation.presentation.viewmodels.WorkerViewModel
import com.workstation.rotation.presentation.viewmodels.WorkstationViewModel
import com.workstation.rotation.presentation.viewmodels.RotationViewModel
import com.workstation.rotation.presentation.viewmodels.HistoryViewModel
import java.io.File

class DesktopAppContainer {
    
    private val databasePath = File(
        System.getProperty("user.home"),
        ".workstation_rotation/database.db"
    ).also { it.parentFile?.mkdirs() }
    
    private val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}").also {
        AppDatabase.Schema.create(it)
    }
    
    private val database = AppDatabase(driver)
    
    val repository = RotationRepository(database)
    
    val rotationService = RotationService(repository)
    
    val workerViewModel = WorkerViewModel(repository)
    
    val workstationViewModel = WorkstationViewModel(repository)
    
    val rotationViewModel = RotationViewModel(repository, rotationService)
    
    val historyViewModel = HistoryViewModel(rotationService)
    
    val exportService = com.workstation.rotation.domain.service.ExportService(repository)
}
