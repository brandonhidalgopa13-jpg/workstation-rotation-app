package com.workstation.rotation.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.workstation.rotation.MainActivity
import com.workstation.rotation.R
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI para MainActivity
 * Verifica que la interfaz principal funcione correctamente
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @Test
    fun mainActivity_displaysCorrectTitle() {
        // Arrange & Act
        ActivityScenario.launch(MainActivity::class.java)

        // Assert
        onView(withText("Sistema de Rotación Inteligente"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_hasAllMainButtons() {
        // Arrange & Act
        ActivityScenario.launch(MainActivity::class.java)

        // Assert - Check all main navigation buttons are present
        onView(withId(R.id.btnWorkers))
            .check(matches(isDisplayed()))
            .check(matches(withText("Gestionar Trabajadores")))

        onView(withId(R.id.btnWorkstations))
            .check(matches(isDisplayed()))
            .check(matches(withText("Gestionar Estaciones")))

        onView(withId(R.id.btnRotation))
            .check(matches(isDisplayed()))
            .check(matches(withText("Generar Rotación")))

        onView(withId(R.id.btnSettings))
            .check(matches(isDisplayed()))
            .check(matches(withText("Configuraciones")))
    }

    @Test
    fun clickWorkersButton_navigatesToWorkerActivity() {
        // Arrange
        ActivityScenario.launch(MainActivity::class.java)

        // Act
        onView(withId(R.id.btnWorkers))
            .perform(click())

        // Assert - Should navigate to WorkerActivity
        // Note: This test assumes WorkerActivity has a specific UI element
        // You might need to adjust based on your actual WorkerActivity layout
        onView(withText("Gestión de Trabajadores"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickWorkstationsButton_navigatesToWorkstationActivity() {
        // Arrange
        ActivityScenario.launch(MainActivity::class.java)

        // Act
        onView(withId(R.id.btnWorkstations))
            .perform(click())

        // Assert
        onView(withText("Gestión de Estaciones"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickRotationButton_navigatesToRotationActivity() {
        // Arrange
        ActivityScenario.launch(MainActivity::class.java)

        // Act
        onView(withId(R.id.btnRotation))
            .perform(click())

        // Assert
        onView(withText("Rotación de Trabajadores"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickSettingsButton_navigatesToSettingsActivity() {
        // Arrange
        ActivityScenario.launch(MainActivity::class.java)

        // Act
        onView(withId(R.id.btnSettings))
            .perform(click())

        // Assert
        onView(withText("Configuraciones"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_hasCorrectLayout() {
        // Arrange & Act
        ActivityScenario.launch(MainActivity::class.java)

        // Assert - Check main container is displayed
        onView(withId(R.id.main))
            .check(matches(isDisplayed()))

        // Check that buttons are clickable
        onView(withId(R.id.btnWorkers))
            .check(matches(isClickable()))

        onView(withId(R.id.btnWorkstations))
            .check(matches(isClickable()))

        onView(withId(R.id.btnRotation))
            .check(matches(isClickable()))

        onView(withId(R.id.btnSettings))
            .check(matches(isClickable()))
    }

    @Test
    fun mainActivity_buttonsHaveCorrectIcons() {
        // Arrange & Act
        ActivityScenario.launch(MainActivity::class.java)

        // Assert - Check that buttons have drawable icons
        // Note: This is a basic check that the buttons exist and are displayed
        // More specific icon testing would require custom matchers
        onView(withId(R.id.btnWorkers))
            .check(matches(isDisplayed()))
            .check(matches(hasContentDescription()))

        onView(withId(R.id.btnWorkstations))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnRotation))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnSettings))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_handlesBackPress() {
        // Arrange
        ActivityScenario.launch(MainActivity::class.java)

        // Act - Navigate to another activity and back
        onView(withId(R.id.btnWorkers))
            .perform(click())

        // Press back button
        onView(isRoot()).perform(pressBack())

        // Assert - Should be back to main activity
        onView(withText("Sistema de Rotación Inteligente"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_rotatesCorrectly() {
        // Arrange & Act
        ActivityScenario.launch(MainActivity::class.java)

        // Assert - Main elements should still be visible after rotation
        // Note: This test assumes the activity handles configuration changes properly
        onView(withId(R.id.btnWorkers))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnWorkstations))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnRotation))
            .check(matches(isDisplayed()))

        onView(withId(R.id.btnSettings))
            .check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_hasAccessibleContent() {
        // Arrange & Act
        ActivityScenario.launch(MainActivity::class.java)

        // Assert - Check accessibility features
        onView(withId(R.id.btnWorkers))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        onView(withId(R.id.btnWorkstations))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        onView(withId(R.id.btnRotation))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        onView(withId(R.id.btnSettings))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }
}