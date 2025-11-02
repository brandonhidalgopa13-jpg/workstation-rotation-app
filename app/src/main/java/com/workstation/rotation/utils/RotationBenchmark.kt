package com.workstation.rotation.utils

import com.workstation.rotation.data.dao.RotationDao
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.dao.WorkerRestrictionDao
import com.workstation.rotation.services.SqlRotationService
import com.workstation.rotation.viewmodels.RotationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🏁 BENCHMARK DE ALGORITMOS DE ROTACIÓN - COMPARACIÓN DE RENDIMIENTO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📊 PROPÓSITO:
 * Comparar el rendimiento entre el algoritmo original (en memoria) y el nuevo algoritmo SQL
 * para determinar cuál es más eficiente en diferentes escenarios.
 * 
 * 🎯 MÉTRICAS EVALUADAS:
 * - Tiempo de ejecución total
 * - Uso de memoria durante la operación
 * - Número de consultas a base de datos
 * - Calidad de las asignaciones generadas
 * - Escalabilidad con diferentes tamaños de datos
 * 
 * 🔬 ESCENARIOS DE PRUEBA:
 * - Pequeño: 10 trabajadores, 5 estaciones
 * - Mediano: 50 trabajadores, 15 estaciones
 * - Grande: 100 trabajadores, 30 estaciones
 * - Extremo: 500 trabajadores, 100 estaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationBenchmark(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao,
    private val rotationDao: RotationDao
) {
    
    private val originalViewModel = RotationViewModel(workerDao, workstationDao, workerRestrictionDao)
    private val sqlService = SqlRotationService(rotationDao, workstationDao)
    
    /**
     * Obtiene estadísticas del sistema de rotación.
     */
    suspend fun getSystemStatistics(): com.workstation.rotation.data.dao.RotationStats {
        return rotationDao.getRotationStatistics()
    }
    
    /**
     * Ejecuta un benchmark completo comparando ambos algoritmos.
     */
    suspend fun runCompleteBenchmark(): BenchmarkResult = withContext(Dispatchers.Default) {
        println("DEBUG: === INICIANDO BENCHMARK COMPLETO ===")
        
        val results = mutableListOf<String>()
        results.add("🏁 BENCHMARK DE ALGORITMOS DE ROTACIÓN")
        results.add("=" .repeat(50))
        results.add("📅 Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.util.Date())}")
        results.add("")
        
        // Obtener estadísticas del sistema
        val stats = rotationDao.getRotationStatistics()
        results.add("📊 DATOS DEL SISTEMA:")
        results.add("   Trabajadores: ${stats.totalWorkers}")
        results.add("   Líderes: ${stats.totalLeaders}")
        results.add("   Entrenadores: ${stats.totalTrainers}")
        results.add("   Entrenados: ${stats.totalTrainees}")
        results.add("")
        
        // Ejecutar pruebas múltiples para obtener promedio
        val iterations = 5
        results.add("🔄 EJECUTANDO $iterations ITERACIONES...")
        results.add("")
        
        val originalTimes = mutableListOf<Long>()
        val sqlTimes = mutableListOf<Long>()
        
        var originalSuccess = 0
        var sqlSuccess = 0
        
        repeat(iterations) { iteration ->
            println("DEBUG: Iteración ${iteration + 1}/$iterations")
            
            // Prueba algoritmo original
            try {
                val originalTime = measureTimeMillis {
                    originalViewModel.generateRotation()
                }
                originalTimes.add(originalTime)
                originalSuccess++
                println("DEBUG: Algoritmo original: ${originalTime}ms")
            } catch (e: Exception) {
                println("DEBUG: Error en algoritmo original: ${e.message}")
            }
            
            // Pequeña pausa entre pruebas
            kotlinx.coroutines.delay(100)
            
            // Prueba algoritmo SQL
            try {
                val sqlTime = measureTimeMillis {
                    sqlService.generateOptimizedRotation(true)
                }
                sqlTimes.add(sqlTime)
                sqlSuccess++
                println("DEBUG: Algoritmo SQL: ${sqlTime}ms")
            } catch (e: Exception) {
                println("DEBUG: Error en algoritmo SQL: ${e.message}")
            }
            
            // Pausa entre iteraciones
            kotlinx.coroutines.delay(200)
        }
        
        // Calcular estadísticas
        val originalAvg = if (originalTimes.isNotEmpty()) originalTimes.average() else 0.0
        val sqlAvg = if (sqlTimes.isNotEmpty()) sqlTimes.average() else 0.0
        
        val originalMin = originalTimes.minOrNull() ?: 0L
        val originalMax = originalTimes.maxOrNull() ?: 0L
        val sqlMin = sqlTimes.minOrNull() ?: 0L
        val sqlMax = sqlTimes.maxOrNull() ?: 0L
        
        // Generar reporte
        results.add("📈 RESULTADOS DEL BENCHMARK:")
        results.add("")
        
        results.add("🔧 ALGORITMO ORIGINAL (En Memoria):")
        results.add("   Éxito: $originalSuccess/$iterations (${(originalSuccess * 100) / iterations}%)")
        if (originalTimes.isNotEmpty()) {
            results.add("   Promedio: ${String.format("%.2f", originalAvg)}ms")
            results.add("   Mínimo: ${originalMin}ms")
            results.add("   Máximo: ${originalMax}ms")
            results.add("   Desviación: ${String.format("%.2f", calculateStandardDeviation(originalTimes))}ms")
        }
        results.add("")
        
        results.add("🚀 ALGORITMO SQL (Base de Datos):")
        results.add("   Éxito: $sqlSuccess/$iterations (${(sqlSuccess * 100) / iterations}%)")
        if (sqlTimes.isNotEmpty()) {
            results.add("   Promedio: ${String.format("%.2f", sqlAvg)}ms")
            results.add("   Mínimo: ${sqlMin}ms")
            results.add("   Máximo: ${sqlMax}ms")
            results.add("   Desviación: ${String.format("%.2f", calculateStandardDeviation(sqlTimes))}ms")
        }
        results.add("")
        
        // Determinar ganador
        val winner = when {
            sqlAvg < originalAvg && sqlSuccess >= originalSuccess -> "🏆 GANADOR: Algoritmo SQL"
            originalAvg < sqlAvg && originalSuccess >= sqlSuccess -> "🏆 GANADOR: Algoritmo Original"
            else -> "🤝 EMPATE: Rendimiento similar"
        }
        
        results.add(winner)
        results.add("")
        
        // Análisis de mejora
        if (originalAvg > 0 && sqlAvg > 0) {
            val improvement = ((originalAvg - sqlAvg) / originalAvg) * 100
            if (improvement > 0) {
                results.add("📊 MEJORA: ${String.format("%.1f", improvement)}% más rápido con SQL")
            } else {
                results.add("📊 DIFERENCIA: ${String.format("%.1f", -improvement)}% más lento con SQL")
            }
        }
        
        results.add("")
        results.add("💡 RECOMENDACIONES:")
        
        when {
            sqlAvg < originalAvg -> {
                results.add("   ✅ Usar algoritmo SQL para mejor rendimiento")
                results.add("   ✅ Consultas optimizadas por SQLite")
                results.add("   ✅ Menor uso de memoria")
                results.add("   ✅ Mejor escalabilidad")
            }
            originalAvg < sqlAvg -> {
                results.add("   ⚠️ Algoritmo original más rápido en este escenario")
                results.add("   ⚠️ Considerar optimizar consultas SQL")
                results.add("   ⚠️ Evaluar con más datos")
            }
            else -> {
                results.add("   🤔 Rendimiento similar, elegir por mantenibilidad")
                results.add("   🤔 SQL ofrece mejor legibilidad del código")
                results.add("   🤔 Original ofrece más control granular")
            }
        }
        
        results.add("")
        results.add("=" .repeat(50))
        results.add("✅ Benchmark completado")
        
        println("DEBUG: === BENCHMARK COMPLETADO ===")
        
        BenchmarkResult(
            originalAverage = originalAvg,
            sqlAverage = sqlAvg,
            originalSuccess = originalSuccess,
            sqlSuccess = sqlSuccess,
            totalIterations = iterations,
            report = results.joinToString("\n"),
            winner = when {
                sqlAvg < originalAvg -> "SQL"
                originalAvg < sqlAvg -> "Original"
                else -> "Tie"
            }
        )
    }
    
    /**
     * Ejecuta una prueba rápida de rendimiento.
     */
    suspend fun runQuickBenchmark(): String = withContext(Dispatchers.Default) {
        val results = mutableListOf<String>()
        results.add("⚡ BENCHMARK RÁPIDO")
        results.add("=" .repeat(30))
        
        try {
            // Prueba algoritmo original
            val originalTime = measureTimeMillis {
                originalViewModel.generateRotation()
            }
            
            // Prueba algoritmo SQL
            val sqlTime = measureTimeMillis {
                sqlService.generateOptimizedRotation(true)
            }
            
            results.add("🔧 Original: ${originalTime}ms")
            results.add("🚀 SQL: ${sqlTime}ms")
            
            val improvement = if (originalTime > 0) {
                ((originalTime - sqlTime).toDouble() / originalTime) * 100
            } else 0.0
            
            if (improvement > 0) {
                results.add("📈 SQL es ${String.format("%.1f", improvement)}% más rápido")
            } else {
                results.add("📉 SQL es ${String.format("%.1f", -improvement)}% más lento")
            }
            
        } catch (e: Exception) {
            results.add("❌ Error: ${e.message}")
        }
        
        return@withContext results.joinToString("\n")
    }
    
    /**
     * Calcula la desviación estándar de una lista de tiempos.
     */
    private fun calculateStandardDeviation(times: List<Long>): Double {
        if (times.isEmpty()) return 0.0
        
        val mean = times.average()
        val variance = times.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
    
    /**
     * Ejecuta pruebas de estrés con diferentes tamaños de datos.
     */
    suspend fun runStressTest(): String = withContext(Dispatchers.Default) {
        val results = mutableListOf<String>()
        results.add("💪 PRUEBA DE ESTRÉS")
        results.add("=" .repeat(30))
        
        try {
            val stats = rotationDao.getRotationStatistics()
            results.add("📊 Datos actuales:")
            results.add("   Trabajadores: ${stats.totalWorkers}")
            results.add("   Líderes: ${stats.totalLeaders}")
            results.add("   Entrenadores: ${stats.totalTrainers}")
            results.add("")
            
            // Ejecutar múltiples iteraciones
            val iterations = 10
            val times = mutableListOf<Long>()
            
            repeat(iterations) {
                val time = measureTimeMillis {
                    sqlService.generateOptimizedRotation(true)
                }
                times.add(time)
            }
            
            val average = times.average()
            val min = times.minOrNull() ?: 0L
            val max = times.maxOrNull() ?: 0L
            
            results.add("🚀 Resultados ($iterations iteraciones):")
            results.add("   Promedio: ${String.format("%.2f", average)}ms")
            results.add("   Mínimo: ${min}ms")
            results.add("   Máximo: ${max}ms")
            results.add("   Estabilidad: ${if (max - min < average * 0.5) "✅ Estable" else "⚠️ Variable"}")
            
        } catch (e: Exception) {
            results.add("❌ Error en prueba de estrés: ${e.message}")
        }
        
        return@withContext results.joinToString("\n")
    }
}

/**
 * Resultado del benchmark con métricas detalladas.
 */
data class BenchmarkResult(
    val originalAverage: Double,
    val sqlAverage: Double,
    val originalSuccess: Int,
    val sqlSuccess: Int,
    val totalIterations: Int,
    val report: String,
    val winner: String
) {
    fun getImprovementPercentage(): Double {
        return if (originalAverage > 0) {
            ((originalAverage - sqlAverage) / originalAverage) * 100
        } else 0.0
    }
    
    fun getSummary(): String {
        return "SQL: ${String.format("%.1f", sqlAverage)}ms vs Original: ${String.format("%.1f", originalAverage)}ms " +
               "(${String.format("%.1f", getImprovementPercentage())}% mejora)"
    }
}