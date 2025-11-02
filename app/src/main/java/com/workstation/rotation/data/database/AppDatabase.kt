package com.workstation.rotation.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.annotation.VisibleForTesting
import android.content.Context
import com.workstation.rotation.utils.Constants
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.dao.RotationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstation
import com.workstation.rotation.data.entities.WorkerRestriction
import com.workstation.rotation.data.dao.WorkerRestrictionDao

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🗄️ BASE DE DATOS PRINCIPAL - NÚCLEO DE PERSISTENCIA DEL SISTEMA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 ESTRUCTURA DE LA BASE DE DATOS:
 * 
 * 🏗️ ENTIDADES PRINCIPALES:
 * 
 * 👤 Worker (Trabajadores):
 *    - Información personal y de contacto
 *    - Sistema de disponibilidad y restricciones
 *    - Configuración de roles de entrenamiento
 *    - Estado activo/inactivo para rotaciones
 * 
 * 🏭 Workstation (Estaciones de Trabajo):
 *    - Definición de estaciones y sus capacidades
 *    - Marcado de estaciones prioritarias
 *    - Estado activo/inactivo para operaciones
 * 
 * 🔗 WorkerWorkstation (Relación Muchos-a-Muchos):
 *    - Vincula trabajadores con estaciones donde pueden trabajar
 *    - Permite flexibilidad en asignaciones
 *    - Base para validaciones del algoritmo de rotación
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔧 CARACTERÍSTICAS TÉCNICAS:
 * 
 * • Room Database con patrón Singleton para eficiencia
 * • DAOs (Data Access Objects) para operaciones especializadas
 * • Migraciones destructivas para desarrollo ágil
 * • Soporte para testing con limpieza de instancia
 * • Versión 5 con esquema optimizado para entrenamiento
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 FLUJO DE DATOS:
 * 
 * 1. UI Activities ↔ ViewModels ↔ DAOs ↔ Database
 * 2. Algoritmo de Rotación consulta todas las entidades
 * 3. Validaciones cruzadas entre trabajadores y estaciones
 * 4. Persistencia automática de cambios y configuraciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Database(
    entities = [Worker::class, Workstation::class, WorkerWorkstation::class, WorkerRestriction::class],
    version = 8, // Incrementar versión para los nuevos campos de liderazgo
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun workerDao(): WorkerDao
    abstract fun workstationDao(): WorkstationDao
    abstract fun workerRestrictionDao(): WorkerRestrictionDao
    abstract fun reportsDao(): com.workstation.rotation.data.dao.ReportsDao
    abstract fun rotationDao(): RotationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // For development - will recreate DB on schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Clears the database instance. Useful for testing.
         */
        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
}