package com.workstation.rotation

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.adapters.BenchmarkResultAdapter
import com.workstation.rotation.models.BenchmarkResult
import kotlinx.coroutines.*

class BenchmarkActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BenchmarkResultAdapter
    private lateinit var btnRunSingle: Button
    private lateinit var btnRunMultiple: Button
    private lateinit var tvStatus: TextView
    
    // ViewModels se inicializarán cuando sea necesario
    
    private val results = mutableListOf<BenchmarkResult>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benchmark)
        
        initViews()
        setupRecyclerView()
        setupClickListeners()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewResults)
        btnRunSingle = findViewById(R.id.btnRunSingleTest)
        btnRunMultiple = findViewById(R.id.btnRunMultipleTests)
        tvStatus = findViewById(R.id.tvProgress)
    }
    
    // ViewModels removidos para simplificar
    
    private fun setupRecyclerView() {
        adapter = BenchmarkResultAdapter(results)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupClickListeners() {
        btnRunSingle.setOnClickListener {
            runSingleBenchmark()
        }
        
        btnRunMultiple.setOnClickListener {
            runMultipleBenchmarks()
        }
    }
    
    private fun runSingleBenchmark() {
        tvStatus.text = "Ejecutando benchmark único..."
        btnRunSingle.isEnabled = false
        btnRunMultiple.isEnabled = false
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val originalResult = measureAlgorithm("Original", ::runOriginalAlgorithm)
                val sqlResult = measureAlgorithm("SQL", ::runSqlAlgorithm)
                
                results.clear()
                results.add(originalResult)
                results.add(sqlResult)
                
                adapter.notifyDataSetChanged()
                
                tvStatus.text = "Benchmark completado"
            } catch (e: Exception) {
                tvStatus.text = "Error en benchmark: ${e.message}"
            } finally {
                btnRunSingle.isEnabled = true
                btnRunMultiple.isEnabled = true
            }
        }
    }
    
    private fun runMultipleBenchmarks() {
        tvStatus.text = "Ejecutando 5 benchmarks..."
        btnRunSingle.isEnabled = false
        btnRunMultiple.isEnabled = false
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val originalResults = mutableListOf<BenchmarkResult>()
                val sqlResults = mutableListOf<BenchmarkResult>()
                
                repeat(5) { iteration ->
                    tvStatus.text = "Ejecutando benchmark ${iteration + 1}/5..."
                    
                    val originalResult = measureAlgorithm("Original #${iteration + 1}", ::runOriginalAlgorithm)
                    val sqlResult = measureAlgorithm("SQL #${iteration + 1}", ::runSqlAlgorithm)
                    
                    originalResults.add(originalResult)
                    sqlResults.add(sqlResult)
                }
                
                // Calcular promedios
                val avgOriginal = calculateAverage(originalResults, "Original (Promedio)")
                val avgSql = calculateAverage(sqlResults, "SQL (Promedio)")
                
                results.clear()
                results.addAll(originalResults)
                results.addAll(sqlResults)
                results.add(avgOriginal)
                results.add(avgSql)
                
                adapter.notifyDataSetChanged()
                
                tvStatus.text = "5 benchmarks completados"
            } catch (e: Exception) {
                tvStatus.text = "Error en benchmarks: ${e.message}"
            } finally {
                btnRunSingle.isEnabled = true
                btnRunMultiple.isEnabled = true
            }
        }
    }
    
    private suspend fun measureAlgorithm(name: String, algorithm: suspend () -> Boolean): BenchmarkResult {
        return withContext(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()
            val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            
            val success = try {
                algorithm()
            } catch (e: Exception) {
                false
            }
            
            val endTime = System.currentTimeMillis()
            val endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            
            BenchmarkResult(
                algorithmName = name,
                executionTimeMs = endTime - startTime,
                memoryUsageMB = maxOf(0.0, (endMemory - startMemory) / (1024.0 * 1024.0)),
                assignmentsCount = getAssignmentsCount(),
                successRate = if (success) 100.0 else 0.0,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    private suspend fun runOriginalAlgorithm(): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                // Simular ejecución del algoritmo original
                delay(100) // Simular tiempo de procesamiento
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private suspend fun runSqlAlgorithm(): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                // Simular ejecución del algoritmo SQL
                delay(50) // Simular tiempo de procesamiento más rápido
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private fun getAssignmentsCount(): Int {
        // Simular conteo de asignaciones
        return (20..50).random()
    }
    
    private fun calculateAverage(results: List<BenchmarkResult>, name: String): BenchmarkResult {
        val avgTime = results.map { it.executionTimeMs }.average().toLong()
        val avgMemory = results.map { it.memoryUsageMB }.average()
        val avgAssignments = results.map { it.assignmentsCount }.average().toInt()
        val avgSuccess = results.map { it.successRate }.average()
        
        return BenchmarkResult(
            algorithmName = name,
            executionTimeMs = avgTime,
            memoryUsageMB = avgMemory,
            assignmentsCount = avgAssignments,
            successRate = avgSuccess,
            timestamp = System.currentTimeMillis()
        )
    }
}