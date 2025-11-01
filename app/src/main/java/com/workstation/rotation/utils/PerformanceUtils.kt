package com.workstation.rotation.utils

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * Utilidades para monitoreo y optimización de rendimiento
 */
object PerformanceUtils {
    
    internal const val TAG = "PerformanceUtils"
    internal const val PERFORMANCE_THRESHOLD_MS = 100L
    
    /**
     * Mide el tiempo de ejecución de una operación
     */
    inline fun <T> measureExecutionTime(
        operationName: String,
        operation: () -> T
    ): T {
        val startTime = SystemClock.elapsedRealtime()
        val result = operation()
        val executionTime = SystemClock.elapsedRealtime() - startTime
        
        if (executionTime > PERFORMANCE_THRESHOLD_MS) {
            Log.w(TAG, "⚠️ Operación lenta detectada: $operationName tomó ${executionTime}ms")
        } else {
            Log.d(TAG, "✅ $operationName completado en ${executionTime}ms")
        }
        
        return result
    }
    
    /**
     * Mide el tiempo de ejecución de una operación suspendida
     */
    suspend inline fun <T> measureSuspendExecutionTime(
        operationName: String,
        crossinline operation: suspend () -> T
    ): T = withContext(Dispatchers.Default) {
        val executionTime = measureTimeMillis {
            operation()
        }
        
        if (executionTime > PERFORMANCE_THRESHOLD_MS) {
            Log.w(TAG, "⚠️ Operación suspendida lenta: $operationName tomó ${executionTime}ms")
        } else {
            Log.d(TAG, "✅ $operationName (suspendida) completado en ${executionTime}ms")
        }
        
        operation()
    }
    
    /**
     * Optimiza listas grandes dividiendo en chunks
     */
    fun <T> optimizeListProcessing(
        list: List<T>,
        chunkSize: Int = 50,
        processor: (List<T>) -> Unit
    ) {
        if (list.size <= chunkSize) {
            processor(list)
            return
        }
        
        Log.d(TAG, "🔄 Procesando lista grande de ${list.size} elementos en chunks de $chunkSize")
        
        list.chunked(chunkSize).forEach { chunk ->
            processor(chunk)
        }
    }
    
    /**
     * Monitorea el uso de memoria
     */
    fun logMemoryUsage(context: String) {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory
        
        val usedMB = usedMemory / (1024 * 1024)
        val maxMB = maxMemory / (1024 * 1024)
        val availableMB = availableMemory / (1024 * 1024)
        
        Log.d(TAG, "📊 Memoria [$context]: Usado=${usedMB}MB, Máximo=${maxMB}MB, Disponible=${availableMB}MB")
        
        if (availableMB < 50) {
            Log.w(TAG, "⚠️ Memoria baja detectada: solo ${availableMB}MB disponibles")
        }
    }
    
    /**
     * Estadísticas de rendimiento para rotaciones
     */
    data class RotationPerformanceStats(
        val totalWorkers: Int,
        val totalWorkstations: Int,
        val executionTimeMs: Long,
        val memoryUsedMB: Long,
        val algorithmsUsed: List<String>
    ) {
        fun getSummary(): String {
            return """
                📊 Estadísticas de Rendimiento:
                👥 Trabajadores procesados: $totalWorkers
                🏭 Estaciones procesadas: $totalWorkstations
                ⏱️ Tiempo de ejecución: ${executionTimeMs}ms
                💾 Memoria utilizada: ${memoryUsedMB}MB
                🔧 Algoritmos: ${algorithmsUsed.joinToString(", ")}
            """.trimIndent()
        }
        
        fun isPerformant(): Boolean {
            return executionTimeMs < PERFORMANCE_THRESHOLD_MS && memoryUsedMB < 100
        }
    }
    
    /**
     * Optimizaciones específicas para el algoritmo de rotación
     */
    object RotationOptimizations {
        
        /**
         * Pre-calcula asignaciones válidas para optimizar el algoritmo
         */
        fun preCalculateValidAssignments(
            workers: List<com.workstation.rotation.data.entities.Worker>,
            workstations: List<com.workstation.rotation.data.entities.Workstation>
        ): Map<Long, List<Long>> {
            return measureExecutionTime("PreCalculateValidAssignments") {
                workers.associate { worker ->
                    worker.id to workstations.filter { workstation ->
                        canWorkerWorkInStation(worker, workstation)
                    }.map { it.id }
                }
            }
        }
        
        private fun canWorkerWorkInStation(
            worker: com.workstation.rotation.data.entities.Worker,
            workstation: com.workstation.rotation.data.entities.Workstation
        ): Boolean {
            // Verificar disponibilidad mínima
            if (worker.availabilityPercentage < 50) {
                return false
            }
            
            return worker.isActive && workstation.isActive
        }
        
        /**
         * Optimiza la asignación de líderes
         */
        fun optimizeLeaderAssignments(
            leaders: List<com.workstation.rotation.data.entities.Worker>,
            workstations: List<com.workstation.rotation.data.entities.Workstation>
        ): Map<Long, Long> {
            return measureExecutionTime("OptimizeLeaderAssignments") {
                leaders.filter { it.isLeader }
                    .mapNotNull { leader ->
                        // Simular asignación de liderazgo
                        workstations.firstOrNull()?.let { station ->
                            leader.id to station.id
                        }
                    }.toMap()
            }
        }
    }
    
    /**
     * Cache para mejorar rendimiento en operaciones repetitivas
     */
    object CacheManager {
        private val cache = mutableMapOf<String, Any>()
        private val cacheTimestamps = mutableMapOf<String, Long>()
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutos
        
        @Suppress("UNCHECKED_CAST")
        fun <T> getOrCompute(key: String, computation: () -> T): T {
            val now = System.currentTimeMillis()
            val timestamp = cacheTimestamps[key] ?: 0
            
            if (cache.containsKey(key) && (now - timestamp) < CACHE_DURATION_MS) {
                Log.d(TAG, "🎯 Cache hit para: $key")
                return cache[key] as T
            }
            
            Log.d(TAG, "🔄 Cache miss, computando: $key")
            val result = computation()
            cache[key] = result as Any
            cacheTimestamps[key] = now
            
            return result
        }
        
        fun clearCache() {
            cache.clear()
            cacheTimestamps.clear()
            Log.d(TAG, "🗑️ Cache limpiado")
        }
        
        fun getCacheStats(): String {
            return "📊 Cache: ${cache.size} entradas, ${cacheTimestamps.size} timestamps"
        }
    }
}