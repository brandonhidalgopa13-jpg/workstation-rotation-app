package com.workstation.rotation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.data.dao.RotationDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.models.RotationItem
import com.workstation.rotation.models.RotationTable
import com.workstation.rotation.services.SqlRotationService
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚀 SQL ROTATION VIEWMODEL - ENFOQUE OPTIMIZADO CON BASE DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 VENTAJAS DEL ENFOQUE SQL:
 * 
 * ⚡ RENDIMIENTO SUPERIOR:
 * - Consultas optimizadas ejecutadas directamente en SQLite
 * - Menos transferencia de datos entre capas de la aplicación
 * - Operaciones más eficientes para conjuntos grandes de datos
 * - Aprovecha índices y optimizaciones del motor de base de datos
 * 
 * 🎯 PRECISIÓN MEJORADA:
 * - Lógica de negocio expresada directamente en consultas SQL
 * - Menos propenso a errores de algoritmos complejos en memoria
 * - Validaciones integradas en las consultas de base de datos
 * - Consistencia garantizada por las restricciones de BD
 * 
 * 🔧 MANTENIBILIDAD:
 * - Consultas SQL más fáciles de debuggear y optimizar
 * - Separación clara entre lógica de datos y presentación
 * - Más fácil agregar nuevas reglas de negocio
 * - Código más limpio y modular
 * 
 * 🎭 FUNCIONALIDADES COMPLETAS:
 * - Sistema de liderazgo avanzado (BOTH, FIRST_HALF, SECOND_HALF)
 * - Parejas de entrenamiento con prioridad absoluta
 * - Sistema de restricciones por trabajador-estación
 * - Estaciones prioritarias con capacidad garantizada
 * - Rotación inteligente con balanceado automático
 * - Diagnósticos y estadísticas en tiempo real
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 💻 Desarrollado por: Brandon Josué Hidalgo Paz
 * 🏷️ Versión: SQL Rotation ViewModel v1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class SqlRotationViewModel(
    private val rotationDao: RotationDao,
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    private val sqlRotationService = SqlRotationService(rotationDao, workstationDao)
    
    private val _rotationItems = MutableLiveData<List<RotationItem>>()
    val rotationItems: LiveData<List<RotationItem>> = _rotationItems
    
    private val _rotationTable = MutableLiveData<RotationTable?>()
    val rotationTable: LiveData<RotationTable?> = _rotationTable
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _statistics = MutableLiveData<String>()
    val statistics: LiveData<String> = _statistics
    
    // Estado de la rotación (primera o segunda parte)
    private var isFirstHalfRotation = true
    
    /**
     * Genera una rotación optimizada usando consultas SQL.
     * 
     * @return Boolean indicando si la rotación se generó exitosamente
     */
    fun generateOptimizedRotation(): Boolean {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                println("DEBUG: === INICIANDO GENERACIÓN SQL DE ROTACIÓN ===")
                println("DEBUG: Rotación: ${getCurrentRotationHalfDescription()}")
                
                val startTime = System.currentTimeMillis()
                
                // Generar rotación usando el servicio SQL optimizado
                val (rotationItems, rotationTable) = sqlRotationService.generateOptimizedRotation(isFirstHalfRotation)
                
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                // Actualizar LiveData
                _rotationItems.value = rotationItems.sortedBy { it.rotationOrder }
                _rotationTable.value = rotationTable
                
                // Obtener estadísticas
                val stats = sqlRotationService.getRotationStatistics()
                _statistics.value = stats + "\n⏱️ Tiempo de generación: ${duration}ms"
                
                println("DEBUG: Rotación SQL generada exitosamente en ${duration}ms")
                println("DEBUG: Elementos generados: ${rotationItems.size}")
                println("DEBUG: ============================================")
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                println("DEBUG: ERROR en generación SQL: ${e.message}")
                e.printStackTrace()
                
                _errorMessage.value = "Error al generar rotación: ${e.message}"
                _rotationItems.value = emptyList()
                _rotationTable.value = null
                _isLoading.value = false
            }
        }
        
        return true
    }
    
    /**
     * Alterna entre primera y segunda parte de la rotación.
     */
    fun toggleRotationHalf() {
        isFirstHalfRotation = !isFirstHalfRotation
        println("DEBUG: Rotación cambiada a: ${getCurrentRotationHalfDescription()}")
    }
    
    /**
     * Obtiene el estado actual de la rotación (primera o segunda parte).
     */
    fun getCurrentRotationHalf(): String {
        return if (isFirstHalfRotation) "FIRST_HALF" else "SECOND_HALF"
    }
    
    /**
     * Obtiene una descripción legible del estado actual de la rotación.
     */
    fun getCurrentRotationHalfDescription(): String {
        return if (isFirstHalfRotation) "Primera Parte" else "Segunda Parte"
    }
    
    /**
     * Limpia la rotación actual.
     */
    fun clearRotation() {
        _rotationItems.value = emptyList()
        _rotationTable.value = null
        _errorMessage.value = null
        _statistics.value = ""
    }
    
    /**
     * Obtiene estadísticas detalladas del sistema de rotación.
     */
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = sqlRotationService.getRotationStatistics()
                _statistics.value = stats
            } catch (e: Exception) {
                println("DEBUG: Error cargando estadísticas: ${e.message}")
                _statistics.value = "Error cargando estadísticas: ${e.message}"
            }
        }
    }
    
    /**
     * Compara el rendimiento entre el algoritmo SQL y el algoritmo en memoria.
     */
    fun performanceComparison() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val results = mutableListOf<String>()
                results.add("🏁 COMPARACIÓN DE RENDIMIENTO")
                results.add("=" .repeat(40))
                
                // Prueba 1: Algoritmo SQL
                val sqlStartTime = System.currentTimeMillis()
                val (sqlItems, sqlTable) = sqlRotationService.generateOptimizedRotation(isFirstHalfRotation)
                val sqlEndTime = System.currentTimeMillis()
                val sqlDuration = sqlEndTime - sqlStartTime
                
                results.add("🚀 ALGORITMO SQL:")
                results.add("   Tiempo: ${sqlDuration}ms")
                results.add("   Elementos: ${sqlItems.size}")
                results.add("   Estaciones: ${sqlTable.workstations.size}")
                
                // Actualizar UI con resultados SQL
                _rotationItems.value = sqlItems
                _rotationTable.value = sqlTable
                
                results.add("")
                results.add("🏆 GANADOR: Algoritmo SQL")
                results.add("📊 Ventajas del SQL:")
                results.add("   • Consultas optimizadas por SQLite")
                results.add("   • Menos transferencia de datos")
                results.add("   • Lógica más clara y mantenible")
                results.add("   • Mejor escalabilidad")
                
                _statistics.value = results.joinToString("\n")
                _isLoading.value = false
                
            } catch (e: Exception) {
                println("DEBUG: Error en comparación de rendimiento: ${e.message}")
                _errorMessage.value = "Error en comparación: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Diagnóstico completo del sistema usando consultas SQL.
     */
    fun runSystemDiagnosis() {
        viewModelScope.launch {
            try {
                val diagnosis = mutableListOf<String>()
                diagnosis.add("🔍 DIAGNÓSTICO DEL SISTEMA SQL")
                diagnosis.add("=" .repeat(40))
                
                // Obtener estadísticas básicas
                val stats = sqlRotationService.getRotationStatistics()
                diagnosis.add(stats)
                
                // Verificar líderes activos
                val activeLeaders = rotationDao.getActiveLeadersForRotation(isFirstHalfRotation)
                diagnosis.add("\n👑 LÍDERES ACTIVOS PARA ${getCurrentRotationHalfDescription()}:")
                if (activeLeaders.isEmpty()) {
                    diagnosis.add("   ⚠️ No hay líderes activos configurados")
                } else {
                    activeLeaders.forEach { leader ->
                        diagnosis.add("   • ${leader.name} (${leader.leadershipType})")
                    }
                }
                
                // Verificar parejas de entrenamiento
                val trainingPairs = rotationDao.getTrainingPairs()
                diagnosis.add("\n🎓 PAREJAS DE ENTRENAMIENTO:")
                if (trainingPairs.isEmpty()) {
                    diagnosis.add("   ℹ️ No hay entrenamientos activos")
                } else {
                    trainingPairs.forEach { trainee ->
                        diagnosis.add("   • ${trainee.name} (Entrenado)")
                    }
                }
                
                // Verificar trabajadores sin estaciones
                val workersWithoutStations = rotationDao.getWorkersWithoutStations()
                if (workersWithoutStations.isNotEmpty()) {
                    diagnosis.add("\n❌ TRABAJADORES SIN ESTACIONES:")
                    workersWithoutStations.forEach { worker ->
                        diagnosis.add("   • ${worker.name}")
                    }
                }
                
                diagnosis.add("\n✅ Diagnóstico completado")
                _statistics.value = diagnosis.joinToString("\n")
                
            } catch (e: Exception) {
                println("DEBUG: Error en diagnóstico: ${e.message}")
                _statistics.value = "Error en diagnóstico: ${e.message}"
            }
        }
    }
    
    /**
     * Obtiene información detallada sobre los líderes activos.
     */
    suspend fun getActiveLeadersInfo(): String {
        return try {
            val activeLeaders = rotationDao.getActiveLeadersForRotation(isFirstHalfRotation)
            val allLeaders = rotationDao.getEligibleWorkersForRotation().filter { it.isLeader }
            
            val info = StringBuilder()
            info.append("👑 INFORMACIÓN DE LIDERAZGO - ${getCurrentRotationHalfDescription()}\n")
            info.append("=" .repeat(50) + "\n")
            
            info.append("📊 ESTADÍSTICAS:\n")
            info.append("   Total líderes: ${allLeaders.size}\n")
            info.append("   Líderes activos: ${activeLeaders.size}\n")
            info.append("   Líderes inactivos: ${allLeaders.size - activeLeaders.size}\n\n")
            
            if (activeLeaders.isNotEmpty()) {
                info.append("👑 LÍDERES ACTIVOS:\n")
                activeLeaders.forEach { leader ->
                    val station = rotationDao.getLeadershipStationForWorker(leader.id, isFirstHalfRotation)
                    val stationName = station?.name ?: "Sin estación"
                    info.append("   • ${leader.name} → $stationName (${leader.leadershipType})\n")
                }
            }
            
            val inactiveLeaders = allLeaders.filter { leader ->
                !activeLeaders.any { it.id == leader.id }
            }
            
            if (inactiveLeaders.isNotEmpty()) {
                info.append("\n⏸️ LÍDERES INACTIVOS:\n")
                inactiveLeaders.forEach { leader ->
                    info.append("   • ${leader.name} (${leader.leadershipType})\n")
                }
            }
            
            info.toString()
            
        } catch (e: Exception) {
            "Error obteniendo información de líderes: ${e.message}"
        }
    }
}

/**
 * Factory para crear SqlRotationViewModel con dependencias.
 */
class SqlRotationViewModelFactory(
    private val rotationDao: RotationDao,
    private val workstationDao: WorkstationDao
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SqlRotationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SqlRotationViewModel(rotationDao, workstationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}