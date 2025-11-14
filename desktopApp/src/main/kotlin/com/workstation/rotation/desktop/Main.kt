package com.workstation.rotation.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.workstation.rotation.App
import com.workstation.rotation.data.DatabaseDriverFactory
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.repository.WorkerRepository
import com.workstation.rotation.domain.repository.WorkstationRepository
import com.workstation.rotation.presentation.viewmodels.WorkerViewModel
import com.workstation.rotation.presentation.viewmodels.WorkstationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun main() = application {
    // Inicializar base de datos
    val driverFactory = DatabaseDriverFactory()
    val database = AppDatabase(driverFactory.createDriver())
    
    // Crear scope para ViewModels
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Crear repositorios
    val workerRepository = WorkerRepository(database)
    val workstationRepository = WorkstationRepository(database)
    
    // Crear ViewModels
    val workerViewModel = WorkerViewModel(workerRepository, scope)
    val workstationViewModel = WorkstationViewModel(workstationRepository, scope)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Rotation"
    ) {
        App(
            workerViewModel = workerViewModel,
            workstationViewModel = workstationViewModel
        )
    }
}
