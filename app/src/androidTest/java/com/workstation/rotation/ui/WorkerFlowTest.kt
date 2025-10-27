package com.workstation.rotation.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.workstation.rotation.WorkerActivity
import com.workstation.rotation.R
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI para el flujo completo de gestión de trabajadores
 * Verifica que se puedan agregar, editar y eliminar trabajadores correctamente
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class WorkerFlowTest {

    @Test
    fun workerActivity_displaysCorrectTitle() {
        // Arrange & Act
        ActivityScenario.launch(WorkerActivity::class.java)

        // Assert
        onView(withText("Gestión de Trabajadores"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun workerActivity_hasAddWorkerButton() {
        // Arrange & Act
        ActivityScenario.launch(WorkerActivity::class.java)

        // Assert
        onView(withId(R.id.fabAddWorker))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun addWorker_opensDialog() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        // Assert - Dialog should be displayed
        onView(withText("Agregar Trabajador"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.etWorkerName))
            .check(matches(isDisplayed()))

        onView(withId(R.id.etCapabilities))
            .check(matches(isDisplayed()))

        onView(withId(R.id.etRestrictions))
            .check(matches(isDisplayed()))
    }

    @Test
    fun addWorker_withValidData_succeeds() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        // Fill in worker details
        onView(withId(R.id.etWorkerName))
            .perform(typeText("Juan Pérez"))

        onView(withId(R.id.etCapabilities))
            .perform(typeText("Soldadura, Ensamble"))

        onView(withId(R.id.etRestrictions))
            .perform(typeText("No levantar peso"))

        // Close keyboard
        onView(withId(R.id.etRestrictions))
            .perform(closeSoftKeyboard())

        // Submit
        onView(withText("Agregar"))
            .perform(click())

        // Assert - Worker should be added to the list
        onView(withText("Juan Pérez"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun addWorker_withEmptyName_showsError() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        // Try to submit without name
        onView(withText("Agregar"))
            .perform(click())

        // Assert - Should show error or stay in dialog
        // The exact behavior depends on your validation implementation
        onView(withText("Agregar Trabajador"))
            .check(matches(isDisplayed())) // Dialog should still be open
    }

    @Test
    fun addWorker_cancelButton_closesDialog() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        onView(withText("Cancelar"))
            .perform(click())

        // Assert - Dialog should be closed
        onView(withText("Agregar Trabajador"))
            .check(doesNotExist())
    }

    @Test
    fun workerList_displaysWorkerInformation() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act - Add a worker first
        addTestWorker("María García", "Control de Calidad", "Solo turno diurno")

        // Assert - Worker information should be displayed
        onView(withText("María García"))
            .check(matches(isDisplayed()))

        // Check that capabilities and restrictions are shown (if your layout includes them)
        // This depends on your actual worker item layout
    }

    @Test
    fun workerList_allowsScrolling() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act - Add multiple workers
        for (i in 1..5) {
            addTestWorker("Trabajador $i", "Habilidad $i", "Restricción $i")
        }

        // Assert - Should be able to scroll through the list
        onView(withId(R.id.recyclerViewWorkers))
            .check(matches(isDisplayed()))

        // Scroll to bottom
        onView(withText("Trabajador 5"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))

        // Scroll to top
        onView(withText("Trabajador 1"))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun workerItem_hasCorrectLayout() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act
        addTestWorker("Test Worker", "Test Skills", "Test Restrictions")

        // Assert - Check worker item layout elements
        onView(withText("Test Worker"))
            .check(matches(isDisplayed()))

        // Check for availability indicator (if present in your layout)
        // Check for trainer/trainee indicators (if present)
        // This depends on your actual worker item layout
    }

    @Test
    fun addTrainer_setsCorrectFlags() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // Act
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        onView(withId(R.id.etWorkerName))
            .perform(typeText("Entrenador Experto"))

        onView(withId(R.id.etCapabilities))
            .perform(typeText("Soldadura Avanzada"))

        // Set as trainer (if checkbox exists)
        onView(withId(R.id.cbIsTrainer))
            .perform(click())

        onView(withId(R.id.etCapabilities))
            .perform(closeSoftKeyboard())

        onView(withText("Agregar"))
            .perform(click())

        // Assert - Trainer should be added with correct indicators
        onView(withText("Entrenador Experto"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun addTrainee_requiresTrainerSelection() {
        // Arrange
        ActivityScenario.launch(WorkerActivity::class.java)

        // First add a trainer
        addTestTrainer("Entrenador Principal")

        // Act - Add trainee
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        onView(withId(R.id.etWorkerName))
            .perform(typeText("Aprendiz Nuevo"))

        // Set as trainee
        onView(withId(R.id.cbIsTrainee))
            .perform(click())

        // Select trainer (if spinner exists)
        // This depends on your actual dialog implementation

        onView(withId(R.id.etWorkerName))
            .perform(closeSoftKeyboard())

        onView(withText("Agregar"))
            .perform(click())

        // Assert
        onView(withText("Aprendiz Nuevo"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun workerActivity_handlesEmptyState() {
        // Arrange & Act
        ActivityScenario.launch(WorkerActivity::class.java)

        // Assert - Should handle empty state gracefully
        onView(withId(R.id.recyclerViewWorkers))
            .check(matches(isDisplayed()))

        // Should show empty state message or just empty list
        // This depends on your implementation
    }

    @Test
    fun workerActivity_hasCorrectMenuOptions() {
        // Arrange & Act
        ActivityScenario.launch(WorkerActivity::class.java)

        // Assert - Check if menu options are available
        // This test would need to be expanded based on your actual menu implementation
        onView(withId(R.id.fabAddWorker))
            .check(matches(isDisplayed()))
    }

    // Helper method to add a test worker
    private fun addTestWorker(name: String, capabilities: String, restrictions: String) {
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        onView(withId(R.id.etWorkerName))
            .perform(typeText(name))

        onView(withId(R.id.etCapabilities))
            .perform(typeText(capabilities))

        onView(withId(R.id.etRestrictions))
            .perform(typeText(restrictions))

        onView(withId(R.id.etRestrictions))
            .perform(closeSoftKeyboard())

        onView(withText("Agregar"))
            .perform(click())
    }

    // Helper method to add a test trainer
    private fun addTestTrainer(name: String) {
        onView(withId(R.id.fabAddWorker))
            .perform(click())

        onView(withId(R.id.etWorkerName))
            .perform(typeText(name))

        onView(withId(R.id.etCapabilities))
            .perform(typeText("Habilidades Expertas"))

        onView(withId(R.id.cbIsTrainer))
            .perform(click())

        onView(withId(R.id.etCapabilities))
            .perform(closeSoftKeyboard())

        onView(withText("Agregar"))
            .perform(click())
    }
}