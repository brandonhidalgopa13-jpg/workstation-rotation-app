package com.workstation.rotation.android.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.repository.RotationRepository
import com.workstation.rotation.domain.service.RotationService
import com.workstation.rotation.presentation.viewmodels.WorkerViewModel
import com.workstation.rotation.presentation.viewmodels.WorkstationViewModel
import com.workstation.rotation.presentation.viewmodels.RotationViewModel
import com.workstation.rotation.presentation.viewmodels.HistoryViewModel

class AppContainer(context: Context) {
    
    private val driver = AndroidSqliteDriver(
        schema = AppDatabase.Schema,
        context = context,
        name = "workstation_rotation.db"
    )
    
    private val database = AppDatabase(driver)
    
    val repository = RotationRepository(database)
    
    val rotationService = RotationService(repository)
    
    val workerViewModel = WorkerViewModel(repository)
    
    val workstationViewModel = WorkstationViewModel(repository)
    
    val rotationViewModel = RotationViewModel(repository, rotationService)
    
    val historyViewModel = HistoryViewModel(rotationService)
    
    val exportService = com.workstation.rotation.domain.service.ExportService(repository)
}
