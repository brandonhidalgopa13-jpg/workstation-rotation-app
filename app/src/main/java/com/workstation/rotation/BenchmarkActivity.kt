package com.workstation.rotation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.databinding.ActivityBenchmarkBinding
import com.workstation.rotation.utils.RotationBenchmark
import com.workstation.rotation.viewmodels.RotationViewModel
import com.workstation.rotation.viewmodels.RotationViewModelFactory
import com.workstation.rotation.viewmodels.SqlRotationViewModel
import com.workstation.rotation.viewmodels.SqlRotationViewModelFactory
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🏁 ACTIVITY DE BENCHMARK - COMPARACIÓN DE ALGORITMOS DE ROTACIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📊 PROPÓSITO:
 * Permite a los usuarios comparar el rendimiento entre el algoritmo original (en memoria)
 * y el nuevo algoritmo SQL para determinar cuál es más eficiente.
 * 
 * 🎯 FUNCIONALIDADES:
 * - Benchmark completo con múltiples iteraciones
 * - Benchmark rápido para pruebas inmediatas
 * - Prueba de estrés para evaluar estabilidad
 * - Comparación lado a lado de ambos algoritmos
 * - Estadísticas detalladas de rendimiento
 * - Recomendaciones basadas en resultados
 * 
 * 🔬 MÉTRICAS EVALUADAS:
 * - Tiempo de ejecución promedio
 * - Tiempo mínimo y máximo
 * - Tasa de éxito de generación
 * - Desviación estándar (estabilidad)
 * - Porcentaje de mejora
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class BenchmarkActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBenchmarkBinding
    private lateinit var database: AppDatabase
    private lateinit var benchmark: RotationBenchmark
    private lateinit var originalViewModel: RotationViewModel
    private lateinit var sqlViewModel: SqlRotationViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBenchmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        initializeDatabase()
        initializeViewModels()
        initializeBenchmark()
        setupClickListeners()
        setupObservers()
        
        // Cargar estadísticas iniciales
        loadInitialStatistics()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Benchmark de Algoritmos"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun initializeDatabase() {
        database = AppDatabase.getDatabase(this)
    }
    
    private fun initializeViewModels() {
        // ViewModel original
        val originalFactory = RotationViewModelFactory(
            database.workerDao(),
            database.workstationDao(),
            database.workerRestrictionDao()
        )
        originalViewModel = ViewModelProvider(this, originalFactory)[RotationViewModel::class.java]
        
        // ViewModel SQL
        val sqlFactory = SqlRotationViewModelFactory(
            database.rotationDao(),
            database.workstationDao()
        )
        sqlViewModel = ViewModelProvider(this, sqlFactory)[SqlRotationViewModel::class.java]
    }
    
    private fun initializeBenchmark() {
        benchmark = RotationBenchmark(
            database.workerDao(),
            database.workstationDao(),
            database.workerRestrictionDao(),
            database.rotationDao()
        )
    }
    
    private fun setupClickListeners() {
        binding.apply {
            btnQuickBenchmark.setOnClickListener { runQuickBenchmark() }
            btnCompleteBenchmark.setOnClickListener { runCompleteBenchmark() }
            btnStressTest.setOnClickListener { runStressTest() }
            btnTestOriginal.setOnClickListener { testOriginalAlgorithm() }
            btnTestSql.setOnClickListener { testSqlAlgorithm() }
            btnClearResults.setOnClickListener { clearResults() }
        }
    }
    
    private fun setupObservers() {
        // Observar ViewModel original
        originalViewModel.rotationItems.observe(this) { items ->
            binding.tvOriginalResults.text = "Algoritmo Original: ${items.size} elementos generados"
        }
        
        // Observar ViewModel SQL
        sqlViewModel.rotationItems.observe(this) { items ->
            binding.tvSqlResults.text = "Algoritmo SQL: ${items.size} elementos generados"
        }
        
        sqlViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            setButtonsEnabled(!isLoading)
        }
        
        sqlViewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
        
        sqlViewModel.statistics.observe(this) { stats ->
            if (stats.isNotEmpty()) {
                binding.tvStatistics.text = stats
            }
        }
    }
    
    private fun loadInitialStatistics() {
        lifecycleScope.launch {
            try {
                val stats = benchmark.rotationDao.getRotationStatistics()
                val initialStats = buildString {
                    appendLine("📊 ESTADÍSTICAS DEL SISTEMA")
                    appendLine("=" .repeat(30))
                    appendLine("👥 Trabajadores: ${stats.totalWorkers}")
                    appendLine("👑 Líderes: ${stats.totalLeaders}")
                    appendLine("👨‍🏫 Entrenadores: ${stats.totalTrainers}")
                    appendLine("🎯 Entrenados: ${stats.totalTrainees}")
                    appendLine("")
                    appendLine("🚀 Listo para benchmark")
                }
                
                binding.tvResults.text = initialStats
                
            } catch (e: Exception) {
                binding.tvResults.text = "Error cargando estadísticas: ${e.message}"
            }
        }
    }
    
    private fun runQuickBenchmark() {
        setButtonsEnabled(false)
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = benchmark.runQuickBenchmark()
                binding.tvResults.text = result
                
            } catch (e: Exception) {
                binding.tvResults.text = "Error en benchmark rápido: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
                setButtonsEnabled(true)
            }
        }
    }
    
    private fun runCompleteBenchmark() {
        setButtonsEnabled(false)
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                binding.tvResults.text = "🔄 Ejecutando benchmark completo...\nEsto puede tomar unos minutos."
                
                val result = benchmark.runCompleteBenchmark()
                binding.tvResults.text = result.report
                
                // Mostrar resumen en toast
                Toast.makeText(
                    this@BenchmarkActivity,
                    result.getSummary(),
                    Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                binding.tvResults.text = "Error en benchmark completo: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
                setButtonsEnabled(true)
            }
        }
    }
    
    private fun runStressTest() {
        setButtonsEnabled(false)
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = benchmark.runStressTest()
                binding.tvResults.text = result
                
            } catch (e: Exception) {
                binding.tvResults.text = "Error en prueba de estrés: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
                setButtonsEnabled(true)
            }
        }
    }
    
    private fun testOriginalAlgorithm() {
        setButtonsEnabled(false)
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val success = originalViewModel.generateRotation()
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                val result = buildString {
                    appendLine("🔧 ALGORITMO ORIGINAL")
                    appendLine("=" .repeat(25))
                    appendLine("⏱️ Tiempo: ${duration}ms")
                    appendLine("✅ Éxito: $success")
                    appendLine("📊 Elementos: ${originalViewModel.rotationItems.value?.size ?: 0}")
                    appendLine("")
                    appendLine("💡 Algoritmo en memoria con lógica compleja")
                }
                
                binding.tvResults.text = result
                
            } catch (e: Exception) {
                binding.tvResults.text = "Error en algoritmo original: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
                setButtonsEnabled(true)
            }
        }
    }
    
    private fun testSqlAlgorithm() {
        setButtonsEnabled(false)
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                val success = sqlViewModel.generateOptimizedRotation()
                val endTime = System.currentTimeMillis()
                val duration = endTime - startTime
                
                val result = buildString {
                    appendLine("🚀 ALGORITMO SQL")
                    appendLine("=" .repeat(20))
                    appendLine("⏱️ Tiempo: ${duration}ms")
                    appendLine("✅ Éxito: $success")
                    appendLine("📊 Elementos: ${sqlViewModel.rotationItems.value?.size ?: 0}")
                    appendLine("")
                    appendLine("💡 Consultas SQL optimizadas por SQLite")
                }
                
                binding.tvResults.text = result
                
            } catch (e: Exception) {
                binding.tvResults.text = "Error en algoritmo SQL: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
                setButtonsEnabled(true)
            }
        }
    }
    
    private fun clearResults() {
        binding.apply {
            tvResults.text = ""
            tvOriginalResults.text = "Algoritmo Original: No ejecutado"
            tvSqlResults.text = "Algoritmo SQL: No ejecutado"
            tvStatistics.text = ""
        }
        
        // Limpiar ViewModels
        originalViewModel.clearRotation()
        sqlViewModel.clearRotation()
        
        // Recargar estadísticas iniciales
        loadInitialStatistics()
    }
    
    private fun setButtonsEnabled(enabled: Boolean) {
        binding.apply {
            btnQuickBenchmark.isEnabled = enabled
            btnCompleteBenchmark.isEnabled = enabled
            btnStressTest.isEnabled = enabled
            btnTestOriginal.isEnabled = enabled
            btnTestSql.isEnabled = enabled
            btnClearResults.isEnabled = enabled
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Limpiar ViewModels
        originalViewModel.clearRotation()
        sqlViewModel.clearRotation()
    }
}