package com.workstation.rotation.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.workstation.rotation.App
import com.workstation.rotation.data.DatabaseDriverFactory
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.repository.WorkerRepository
import com.workstation.rotation.domain.repository.WorkstationRepository
import com.workstation.rotation.presentation.viewmodels.WorkerViewModel
import com.workstation.rotation.presentation.viewmodels.WorkstationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar base de datos
        val driverFactory = DatabaseDriverFactory(applicationContext)
        val database = AppDatabase(driverFactory.createDriver())
        
        // Crear repositorios
        val workerRepository = WorkerRepository(database)
        val workstationRepository = WorkstationRepository(database)
        
        // Crear ViewModels con lifecycleScope
        val workerViewModel = WorkerViewModel(workerRepository, lifecycleScope)
        val workstationViewModel = WorkstationViewModel(workstationRepository, lifecycleScope)
        
        setContent {
            App(
                workerViewModel = workerViewModel,
                workstationViewModel = workstationViewModel
            )
        }
    }
}
