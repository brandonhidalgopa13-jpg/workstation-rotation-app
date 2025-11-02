package com.workstation.rotation.notifications

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.workstation.rotation.R
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * ⚙️ ACTIVIDAD DE CONFIGURACIÓN DE NOTIFICACIONES - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Permite a los usuarios personalizar completamente el sistema de notificaciones inteligentes.
 * Incluye configuración de tipos, timing, horarios y umbrales de alertas.
 * 
 * 🎯 FUNCIONALIDADES:
 * • Habilitar/deshabilitar tipos específicos de notificaciones
 * • Configurar frecuencias y horarios personalizados
 * • Establecer umbrales de alertas según necesidades
 * • Probar notificaciones en tiempo real
 * • Restaurar configuración por defecto
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class NotificationSettingsActivity : AppCompatActivity() {
    
    private lateinit var preferences: NotificationPreferences
    private lateinit var notificationSystem: IntelligentNotificationSystem
    
    // Views - Switches
    private lateinit var switchRotationReminders: SwitchMaterial
    private lateinit var switchCapacityAlerts: SwitchMaterial
    private lateinit var switchTrainingUpdates: SwitchMaterial
    private lateinit var switchWeeklyReports: SwitchMaterial
    private lateinit var switchProactiveAlerts: SwitchMaterial
    private lateinit var switchQuietHours: SwitchMaterial
    
    // Views - Sliders
    private lateinit var sliderRotationHours: Slider
    private lateinit var sliderCapacityMinutes: Slider
    
    // Views - Text
    private lateinit var tvRotationHoursValue: TextView
    private lateinit var tvCapacityMinutesValue: TextView
    
    // Views - Buttons
    private lateinit var btnWorkStartTime: MaterialButton
    private lateinit var btnWorkEndTime: MaterialButton
    private lateinit var btnTestNotification: MaterialButton
    private lateinit var btnResetDefaults: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)
        
        preferences = NotificationPreferences(this)
        notificationSystem = IntelligentNotificationSystem(this)
        
        setupToolbar()
        initializeViews()
        loadCurrentSettings()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración de Notificaciones"
    }
    
    private fun initializeViews() {
        // Switches
        switchRotationReminders = findViewById(R.id.switchRotationReminders)
        switchCapacityAlerts = findViewById(R.id.switchCapacityAlerts)
        switchTrainingUpdates = findViewById(R.id.switchTrainingUpdates)
        switchWeeklyReports = findViewById(R.id.switchWeeklyReports)
        switchProactiveAlerts = findViewById(R.id.switchProactiveAlerts)
        switchQuietHours = findViewById(R.id.switchQuietHours)
        
        // Sliders
        sliderRotationHours = findViewById(R.id.sliderRotationHours)
        sliderCapacityMinutes = findViewById(R.id.sliderCapacityMinutes)
        
        // Text Views
        tvRotationHoursValue = findViewById(R.id.tvRotationHoursValue)
        tvCapacityMinutesValue = findViewById(R.id.tvCapacityMinutesValue)
        
        // Buttons
        btnWorkStartTime = findViewById(R.id.btnWorkStartTime)
        btnWorkEndTime = findViewById(R.id.btnWorkEndTime)
        btnTestNotification = findViewById(R.id.btnTestNotification)
        btnResetDefaults = findViewById(R.id.btnResetDefaults)
    }
    
    private fun loadCurrentSettings() {
        // Cargar switches
        switchRotationReminders.isChecked = preferences.rotationRemindersEnabled
        switchCapacityAlerts.isChecked = preferences.capacityAlertsEnabled
        switchTrainingUpdates.isChecked = preferences.trainingUpdatesEnabled
        switchWeeklyReports.isChecked = preferences.weeklyReportsEnabled
        switchProactiveAlerts.isChecked = preferences.proactiveAlertsEnabled
        switchQuietHours.isChecked = preferences.quietHoursEnabled
        
        // Cargar sliders
        sliderRotationHours.value = preferences.rotationReminderHours.toFloat()
        sliderCapacityMinutes.value = preferences.capacityCheckMinutes.toFloat()
        
        // Actualizar textos de valores
        updateSliderTexts()
        
        // Cargar horarios de trabajo
        updateWorkTimeButtons()
    }
    
    private fun setupListeners() {
        // Switches listeners
        switchRotationReminders.setOnCheckedChangeListener { _, isChecked ->
            preferences.rotationRemindersEnabled = isChecked
            showToast("Recordatorios de rotación ${if (isChecked) "activados" else "desactivados"}")
        }
        
        switchCapacityAlerts.setOnCheckedChangeListener { _, isChecked ->
            preferences.capacityAlertsEnabled = isChecked
            showToast("Alertas de capacidad ${if (isChecked) "activadas" else "desactivadas"}")
        }
        
        switchTrainingUpdates.setOnCheckedChangeListener { _, isChecked ->
            preferences.trainingUpdatesEnabled = isChecked
            showToast("Actualizaciones de entrenamiento ${if (isChecked) "activadas" else "desactivadas"}")
        }
        
        switchWeeklyReports.setOnCheckedChangeListener { _, isChecked ->
            preferences.weeklyReportsEnabled = isChecked
            showToast("Reportes semanales ${if (isChecked) "activados" else "desactivados"}")
        }
        
        switchProactiveAlerts.setOnCheckedChangeListener { _, isChecked ->
            preferences.proactiveAlertsEnabled = isChecked
            showToast("Alertas proactivas ${if (isChecked) "activadas" else "desactivadas"}")
        }
        
        switchQuietHours.setOnCheckedChangeListener { _, isChecked ->
            preferences.quietHoursEnabled = isChecked
            showToast("Horarios de trabajo ${if (isChecked) "respetados" else "ignorados"}")
        }
        
        // Sliders listeners
        sliderRotationHours.addOnChangeListener { _, value, _ ->
            preferences.rotationReminderHours = value.toLong()
            updateSliderTexts()
        }
        
        sliderCapacityMinutes.addOnChangeListener { _, value, _ ->
            preferences.capacityCheckMinutes = value.toLong()
            updateSliderTexts()
        }
        
        // Buttons listeners
        btnWorkStartTime.setOnClickListener { showTimePickerForWorkStart() }
        btnWorkEndTime.setOnClickListener { showTimePickerForWorkEnd() }
        btnTestNotification.setOnClickListener { showTestNotification() }
        btnResetDefaults.setOnClickListener { resetToDefaults() }
    }
    
    private fun updateSliderTexts() {
        val rotationHours = sliderRotationHours.value.toInt()
        val capacityMinutes = sliderCapacityMinutes.value.toInt()
        
        tvRotationHoursValue.text = "$rotationHours ${if (rotationHours == 1) "hora" else "horas"}"
        tvCapacityMinutesValue.text = "$capacityMinutes ${if (capacityMinutes == 1) "minuto" else "minutos"}"
    }
    
    private fun updateWorkTimeButtons() {
        val startHour = preferences.workStartHour
        val endHour = preferences.workEndHour
        
        btnWorkStartTime.text = String.format("%02d:00", startHour)
        btnWorkEndTime.text = String.format("%02d:00", endHour)
    }
    
    private fun showTimePickerForWorkStart() {
        val currentHour = preferences.workStartHour
        
        TimePickerDialog(this, { _, hourOfDay, _ ->
            preferences.workStartHour = hourOfDay
            updateWorkTimeButtons()
            showToast("Horario de inicio actualizado: ${String.format("%02d:00", hourOfDay)}")
        }, currentHour, 0, true).show()
    }
    
    private fun showTimePickerForWorkEnd() {
        val currentHour = preferences.workEndHour
        
        TimePickerDialog(this, { _, hourOfDay, _ ->
            preferences.workEndHour = hourOfDay
            updateWorkTimeButtons()
            showToast("Horario de fin actualizado: ${String.format("%02d:00", hourOfDay)}")
        }, currentHour, 0, true).show()
    }
    
    private fun showTestNotification() {
        // Mostrar diferentes tipos de notificaciones de prueba
        val testOptions = arrayOf(
            "Recordatorio de Rotación",
            "Alerta de Capacidad",
            "Actualización de Entrenamiento",
            "Reporte Semanal",
            "Alerta Proactiva"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Probar Notificación")
            .setItems(testOptions) { _, which ->
                when (which) {
                    0 -> testRotationReminder()
                    1 -> testCapacityAlert()
                    2 -> testTrainingUpdate()
                    3 -> testWeeklyReport()
                    4 -> testProactiveAlert()
                }
            }
            .show()
    }
    
    private fun testRotationReminder() {
        notificationSystem.showRotationReminder(
            hoursActive = 5L,
            activeRotations = 8,
            predictedOptimalTime = "Momento óptimo para rotación (PRUEBA)"
        )
        showToast("Notificación de recordatorio enviada")
    }
    
    private fun testCapacityAlert() {
        notificationSystem.showCapacityAlert(
            stationName = "Estación de Prueba",
            currentCapacity = 4,
            requiredCapacity = 5,
            utilizationPercent = 0.85
        )
        showToast("Alerta de capacidad enviada")
    }
    
    private fun testTrainingUpdate() {
        notificationSystem.showTrainingCompleted(
            traineeName = "Juan Pérez (PRUEBA)",
            trainerName = "María García (PRUEBA)",
            stationName = "Estación A1 (PRUEBA)",
            totalHours = 40,
            finalScore = 8.5
        )
        showToast("Notificación de entrenamiento enviada")
    }
    
    private fun testWeeklyReport() {
        notificationSystem.showWeeklyReport(
            totalRotations = 45,
            averageDuration = 240.5,
            topPerformer = "Ana López (PRUEBA)",
            topPerformanceScore = 9.2,
            totalTrainingsCompleted = 3,
            efficiencyTrend = "up"
        )
        showToast("Reporte semanal enviado")
    }
    
    private fun testProactiveAlert() {
        notificationSystem.showProactiveAlert(
            "🔮 Alerta de Prueba",
            "Esta es una notificación de prueba del sistema proactivo",
            "Esta es solo una prueba para verificar el funcionamiento del sistema.",
            IntelligentNotificationSystem.ProactiveAlertType.NO_ACTIVE_ROTATIONS
        )
        showToast("Alerta proactiva enviada")
    }
    
    private fun resetToDefaults() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Restaurar Configuración")
            .setMessage("¿Desea restaurar toda la configuración a los valores por defecto?")
            .setPositiveButton("Restaurar") { _, _ ->
                preferences.resetToDefaults()
                loadCurrentSettings()
                showToast("Configuración restaurada a valores por defecto")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onPause() {
        super.onPause()
        // Reiniciar workers con nueva configuración
        notificationSystem.cancelAllScheduledNotifications()
        
        // Solo reiniciar si las notificaciones están habilitadas
        if (notificationSystem.areNotificationsEnabled()) {
            val newNotificationSystem = IntelligentNotificationSystem(this)
            // Los workers se reiniciarán automáticamente con la nueva configuración
        }
    }
}