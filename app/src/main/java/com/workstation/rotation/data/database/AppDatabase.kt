package com.workstation.rotation.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstation

@Database(
    entities = [Worker::class, Workstation::class, WorkerWorkstation::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun workerDao(): WorkerDao
    abstract fun workstationDao(): WorkstationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workstation_rotation_database"
                )
                .fallbackToDestructiveMigration() // Para desarrollo - recreará la DB
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}