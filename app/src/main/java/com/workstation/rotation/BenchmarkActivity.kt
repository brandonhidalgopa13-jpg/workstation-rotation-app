package com.workstation.rotation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.workstation.rotation.adapters.BenchmarkResultAdapter
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.databinding.ActivityBenchmarkBinding
import com.workstation.rotation.models.BenchmarkResult
import com.workstation.rotation.viewmodels.RotationViewModel
import com.workstation.rotation.viewmodels.RotationViewModelFactory
import com.workstation.rotation.viewmodels.SqlRotationViewModel
import com.workstation.rotation.viewmodels.SqlRotationViewModelFactory
import com.workstation.rotation.notifications.SmartNotificationManager
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🏁 ACTIVIDAD DE BENCHMARK PARA COMPARACIÓN DE ALGORITMOS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Permite comparar el rendimiento entre:
 * 1. Algoritmo Original (en memoria)
 * 2. Algoritmo SQL Optimizado
 * 
 * Métricas evaluadas:
 * - Tiempo de ejecución
 * - Uso de memoria
 * - Calidad de asignaciones
 * - Consistencia de resultados
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class BenchmarkActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBenchmarkBinding
    private lateinit var benchmarkAdapter: BenchmarkResultAdapter
    private lateinit var notificationManager: SmartNotificationManager
    private val benchmarkResults = mutableListOf<BenchmarkResult>()
    
    private val originalViewModel: RotationViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        RotationViewModelFactory(
            database.workerDao(),
            database.workstationDao(),
            database.rotationItemDao()
        )
    }
    
    private val sqlViewModel: SqlRotationViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        SqlRotationViewModelFactory(
            database.rotationDao(),
            database.workerDao(),
            database.workstationDao()
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBenchmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        notificationManager = SmartNotificationManager(this)
        
        setupToolbar()
        setupRecyclerView()
        setupButtons()
        loadInitialData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "🏁 Benchmark de Algoritmos"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        benchmarkAdapter = BenchmarkResultAdapter(benchmarkResults)
        binding.recyclerViewResults.apply {
            layoutManager = LinearLayoutManager(this@BenchmarkActivity)
            adapter = benchmarkAdapter
        }
    }
    
    private fun setupButtons() {
        binding.btnRunSingleTest.setOnClickListener {
            runSingleBenchmark()
        }
        
        binding.btnRunMultipleTests.setOnClickListener {
            runMultipleBenchmarks()
        }
        
        binding.btnClearResults.setOnClickListener {
            clearResults()
        }
        
        binding.btnExportResults.setOnClickListener {
            exportResults()
        }
    }
    
    private fun loadInitialData() {
        lifecycleScope.launch {
            updateDataInfo()
        }
    }
    
    private fun runSingleBenchmark() {
        lifecycleScope.launch {
            setLoadingState(true)
            
            try {
                val originalResult = benchmarkOriginalAlgorithm()
                val sqlResult = benchmarkSqlAlgorithm()
                
                benchmarkResults.add(originalResult)
                benchmarkResults.add(sqlResult)
                
                benchmarkAdapter.notifyDataSetChanged()
                
                showBenchmarkSummary(originalResult, sqlResult)
                
                // Enviar notificación de resultados
                notificationManager.notifyBenchmarkResults(originalResult, sqlResult)
                
            } catch (e: Exception) {
                showError("Error en benchmark: ${e.message}")
            } finally {
                setLoadingState(false)
            }
        }
    }
    
    private fun runMultipleBenchmarks() {
        lifecycleScope.launch {
            setLoadingState(true)
            
            try {
                val iterations = 5
                val originalTimes = mutableListOf<Long>()
                val sqlTimes = mutableListOf<Long>()
                
                repeat(iterations) { iteration ->
                    updateProgress("Ejecutando iteración ${iteration + 1}/$iterations...")
                    
                    val originalResult = benchmarkOriginalAlgorithm()
                    val sqlResult = benchmarkSqlAlgorithm()
                    
                    originalTimes.add(originalResult.executionTimeMs)
                    sqlTimes.add(sqlResult.executionTimeMs)
                    
                    if (iteration == 0) {
                        // Solo agregar los primeros resultados para mostrar
                        benchmarkResults.add(originalResult)
                        benchmarkResults.add(sqlResult)
                    }
                }
                
                // Crear resultado promedio
                val avgOriginal = BenchmarkResult(
                    algorithmName = "Algoritmo Original (Promedio)",
                    executionTimeMs = originalTimes.average().toLong(),
                    memoryUsageMB = 0.0,
                    assignmentsCount = 0,
                    successRate = 100.0,
                    timestamp = System.currentTimeMillis()
                )
                
                val avgSql = BenchmarkResult(
                    algorithmName = "Algoritmo SQL (Promedio)",
                    executionTimeMs = sqlTimes.average().toLong(),
                    memoryUsageMB = 0.0,
                    assignmentsCount = 0,
                    successRate = 100.0,
                    timestamp = System.currentTimeMillis()
                )
                
                benchmarkResults.add(avgOriginal)
                benchmarkResults.add(avgSql)
                
                benchmarkAdapter.notifyDataSetChanged()
                
                showMultipleBenchmarkSummary(originalTimes, sqlTimes)
                
            } catch (e: Exception) {
                showError("Error en benchmark múltiple: ${e.message}")
            } finally {
                setLoadingState(false)
                updateProgress("")
            }
        }
    }
    
    private suspend fun benchmarkOriginalAlgorithm(): BenchmarkResult {
        val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        val executionTime = measureTimeMillis {
            originalViewModel.generateRotation()
        }
        
        val endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryUsed = (endMemory - startMemory) / (1024.0 * 1024.0) // MB
        
        val assignmentsCount = originalViewModel.rotationItems.value?.size ?: 0
        
        return BenchmarkResult(
            algorithmName = "Algoritmo Original",
            executionTimeMs = executionTime,
            memoryUsageMB = memoryUsed,
            assignmentsCount = assignmentsCount,
            successRate = if (assignmentsCount > 0) 100.0 else 0.0,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun benchmarkSqlAlgorithm(): BenchmarkResult {
        val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        val executionTime = measureTimeMillis {
            sqlViewModel.generateOptimizedRotation()
        }
        
        val endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryUsed = (endMemory - startMemory) / (1024.0 * 1024.0) // MB
        
        val assignmentsCount = sqlViewModel.rotationItems.value?.size ?: 0
        
        return BenchmarkResult(
            algorithmName = "Algoritmo SQL",
            executionTimeMs = executionTime,
            memoryUsageMB = memoryUsed,
            assignmentsCount = assignmentsCount,
            successRate = if (assignmentsCount > 0) 100.0 else 0.0,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun showBenchmarkSummary(original: BenchmarkResult, sql: BenchmarkResult) {
        val improvement = ((original.executionTimeMs - sql.executionTimeMs).toDouble() / original.executionTimeMs * 100)
        val message = if (improvement > 0) {
            "🚀 SQL es ${improvement.toInt()}% más rápido"
        } else {
            "⚡ Original es ${(-improvement).toInt()}% más rápido"
        }
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.status_success))
            .show()
    }
    
    private fun showMultipleBenchmarkSummary(originalTimes: List<Long>, sqlTimes: List<Long>) {
        val avgOriginal = originalTimes.average()
        val avgSql = sqlTimes.average()
        val improvement = ((avgOriginal - avgSql) / avgOriginal * 100)
        
        val message = if (improvement > 0) {
            "🏆 SQL promedio ${improvement.toInt()}% más rápido (${originalTimes.size} iteraciones)"
        } else {
            "🏆 Original promedio ${(-improvement).toInt()}% más rápido (${originalTimes.size} iteraciones)"
        }
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.status_success))
            .show()
    }
    
    private fun clearResults() {
        benchmarkResults.clear()
        benchmarkAdapter.notifyDataSetChanged()
        
        Snackbar.make(binding.root, "🧹 Resultados limpiados", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun exportResults() {
        if (benchmarkResults.isEmpty()) {
            Snackbar.make(binding.root, "❌ No hay resultados para exportar", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        // Implementar exportación a CSV o JSON
        Snackbar.make(binding.root, "📊 Exportación disponible próximamente", Snackbar.LENGTH_SHORT).show()
    }
    
    private suspend fun updateDataInfo() {
        val workerCount = originalViewModel.getAllWorkers().size
        val workstationCount = originalViewModel.getAllWorkstations().size
        
        binding.tvDataInfo.text = "📊 Datos: $workerCount trabajadores, $workstationCount estaciones"
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRunSingleTest.isEnabled = !isLoading
        binding.btnRunMultipleTests.isEnabled = !isLoading
    }
    
    private fun updateProgress(message: String) {
        binding.tvProgress.text = message
        binding.tvProgress.visibility = if (message.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.status_error))
            .show()
    }
}