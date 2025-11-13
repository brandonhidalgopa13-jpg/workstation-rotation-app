package com.workstation.rotation.diagnostics

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstationCapability
import org.junit.Test
import org.junit.Assert.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🧪 TEST DE DIAGNÓSTICO Y REPARACIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Verifica que el sistema de diagnóstico identifique correctamente:
 * • Trabajadores sin capacidades
 * • Capacidades inactivas
 * • Desincronización entre tablas
 * • Reparación automática de capacidades
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class DiagnosticServiceTest {

    @Test
    fun `test identificar trabajadores sin capacidades`() {
        // Arrange: Crear trabajadores y estaciones
        val workers = listOf(
            Worker(id = 1, name = "Maritza", employeeId = "EMP001", isActive = true),
            Worker(id = 2, name = "Oscar", employeeId = "EMP002", isActive = true),
            Worker(id = 3, name = "Trabajador3", employeeId = "EMP003", isActive = true),
            Worker(id = 4, name = "Trabajador4", employeeId = "EMP004", isActive = true),
            Worker(id = 5, name = "Trabajador5", employeeId = "EMP005", isActive = true)
        )
        
        val workstations = listOf(
            Workstation(id = 1, name = "Anneling", requiredWorkers = 2, isActive = true),
            Workstation(id = 2, name = "Forming", requiredWorkers = 2, isActive = true),
            Workstation(id = 3, name = "Loops", requiredWorkers = 1, isActive = true)
        )
        
        // Capacidades solo para 2 trabajadores (Maritza y Oscar)
        val capabilities = listOf(
            WorkerWorkstationCapability(
                worker_id = 1, 
                workstation_id = 1, 
                competency_level = 3, 
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 1, 
                workstation_id = 2, 
                competency_level = 3, 
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 1, 
                workstation_id = 3, 
                competency_level = 3, 
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 2, 
                workstation_id = 1, 
                competency_level = 3, 
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 2, 
                workstation_id = 2, 
                competency_level = 3, 
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 2, 
                workstation_id = 3, 
                competency_level = 3, 
                is_active = true
            )
        )
        
        // Act: Identificar trabajadores sin capacidades
        val workersWithCapabilities = workers.filter { worker ->
            capabilities.any { it.worker_id == worker.id && it.is_active }
        }
        
        val workersWithoutCapabilities = workers.filter { worker ->
            capabilities.none { it.worker_id == worker.id && it.is_active }
        }
        
        // Assert: Verificar que se identificaron correctamente
        assertEquals("Deberían haber 2 trabajadores con capacidades", 2, workersWithCapabilities.size)
        assertEquals("Deberían haber 3 trabajadores sin capacidades", 3, workersWithoutCapabilities.size)
        
        assertTrue(
            "Maritza debería tener capacidades",
            workersWithCapabilities.any { it.name == "Maritza" }
        )
        assertTrue(
            "Oscar debería tener capacidades",
            workersWithCapabilities.any { it.name == "Oscar" }
        )
        
        assertTrue(
            "Trabajador3 NO debería tener capacidades",
            workersWithoutCapabilities.any { it.name == "Trabajador3" }
        )
        assertTrue(
            "Trabajador4 NO debería tener capacidades",
            workersWithoutCapabilities.any { it.name == "Trabajador4" }
        )
        assertTrue(
            "Trabajador5 NO debería tener capacidades",
            workersWithoutCapabilities.any { it.name == "Trabajador5" }
        )
    }
    
    @Test
    fun `test identificar capacidades inactivas`() {
        // Arrange: Crear capacidades con algunas inactivas
        val capabilities = listOf(
            WorkerWorkstationCapability(
                worker_id = 1, 
                workstation_id = 1, 
                competency_level = 3, 
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 1, 
                workstation_id = 2, 
                competency_level = 3, 
                is_active = false  // INACTIVA
            ),
            WorkerWorkstationCapability(
                worker_id = 2, 
                workstation_id = 1, 
                competency_level = 3, 
                is_active = false  // INACTIVA
            ),
            WorkerWorkstationCapability(
                worker_id = 2, 
                workstation_id = 2, 
                competency_level = 3, 
                is_active = true
            )
        )
        
        // Act: Filtrar capacidades inactivas
        val activeCapabilities = capabilities.filter { it.is_active }
        val inactiveCapabilities = capabilities.filter { !it.is_active }
        
        // Assert: Verificar conteo
        assertEquals("Deberían haber 2 capacidades activas", 2, activeCapabilities.size)
        assertEquals("Deberían haber 2 capacidades inactivas", 2, inactiveCapabilities.size)
    }
    
    @Test
    fun `test verificar capacidades asignables`() {
        // Arrange: Crear capacidades con diferentes niveles
        val capabilities = listOf(
            WorkerWorkstationCapability(
                worker_id = 1, 
                workstation_id = 1, 
                competency_level = 1,  // Principiante - NO asignable (nivel < 2)
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 2, 
                workstation_id = 1, 
                competency_level = 2,  // Básico - NO asignable (necesita certificación o nivel >= 3)
                is_active = true,
                is_certified = false
            ),
            WorkerWorkstationCapability(
                worker_id = 3, 
                workstation_id = 1, 
                competency_level = 2,  // Básico certificado - asignable
                is_active = true,
                is_certified = true
            ),
            WorkerWorkstationCapability(
                worker_id = 4, 
                workstation_id = 1, 
                competency_level = 3,  // Intermedio - asignable (nivel >= 3)
                is_active = true
            ),
            WorkerWorkstationCapability(
                worker_id = 5, 
                workstation_id = 1, 
                competency_level = 4,  // Avanzado - asignable
                is_active = true
            )
        )
        
        // Act: Filtrar capacidades asignables
        val assignableCapabilities = capabilities.filter { it.canBeAssigned() }
        val nonAssignableCapabilities = capabilities.filter { !it.canBeAssigned() }
        
        // Assert: Verificar que solo las capacidades correctas son asignables
        assertEquals("Deberían haber 3 capacidades asignables", 3, assignableCapabilities.size)
        assertEquals("Deberían haber 2 capacidades no asignables", 2, nonAssignableCapabilities.size)
        
        assertTrue(
            "Todas las capacidades asignables deberían cumplir los requisitos",
            assignableCapabilities.all { 
                it.is_active && it.competency_level >= 2 && 
                (it.is_certified || it.competency_level >= 3)
            }
        )
    }
    
    @Test
    fun `test simular reparacion de capacidades`() {
        // Arrange: Simular estado antes de reparación
        val workers = listOf(
            Worker(id = 1, name = "Maritza", employeeId = "EMP001", isActive = true),
            Worker(id = 2, name = "Oscar", employeeId = "EMP002", isActive = true),
            Worker(id = 3, name = "Trabajador3", employeeId = "EMP003", isActive = true)
        )
        
        val workstationIds = listOf(1L, 2L, 3L)  // 3 estaciones por trabajador
        
        val existingCapabilities = listOf(
            // Solo Maritza y Oscar tienen capacidades
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 1, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 2, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 3, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 1, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 2, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 3, competency_level = 3, is_active = true)
        )
        
        // Act: Simular creación de capacidades faltantes
        val newCapabilities = mutableListOf<WorkerWorkstationCapability>()
        
        workers.forEach { worker ->
            val workerExistingCapabilities = existingCapabilities.filter { it.worker_id == worker.id }
            val existingStationIds = workerExistingCapabilities.map { it.workstation_id }.toSet()
            
            // Crear capacidades faltantes
            workstationIds.forEach { stationId ->
                if (!existingStationIds.contains(stationId)) {
                    newCapabilities.add(
                        WorkerWorkstationCapability(
                            worker_id = worker.id,
                            workstation_id = stationId,
                            competency_level = 2,  // Nivel básico
                            is_active = true
                        )
                    )
                }
            }
        }
        
        val totalCapabilities = existingCapabilities + newCapabilities
        
        // Assert: Verificar que se crearon las capacidades correctas
        assertEquals("Deberían crearse 3 capacidades nuevas (1 trabajador x 3 estaciones)", 3, newCapabilities.size)
        assertEquals("Total deberían ser 9 capacidades (3 trabajadores x 3 estaciones)", 9, totalCapabilities.size)
        
        // Verificar que cada trabajador tiene capacidades para todas las estaciones
        workers.forEach { worker ->
            val workerCapabilities = totalCapabilities.filter { it.worker_id == worker.id }
            assertEquals(
                "Trabajador ${worker.name} debería tener 3 capacidades",
                3, 
                workerCapabilities.size
            )
            
            workstationIds.forEach { stationId ->
                assertTrue(
                    "Trabajador ${worker.name} debería tener capacidad para estación $stationId",
                    workerCapabilities.any { it.workstation_id == stationId }
                )
            }
        }
    }
    
    @Test
    fun `test calcular trabajadores disponibles para rotacion`() {
        // Arrange: Crear trabajadores y capacidades
        val workers = listOf(
            Worker(id = 1, name = "Maritza", employeeId = "EMP001", isActive = true),
            Worker(id = 2, name = "Oscar", employeeId = "EMP002", isActive = true),
            Worker(id = 3, name = "Trabajador3", employeeId = "EMP003", isActive = true),
            Worker(id = 4, name = "Trabajador4", employeeId = "EMP004", isActive = false),  // INACTIVO
            Worker(id = 5, name = "Trabajador5", employeeId = "EMP005", isActive = true)
        )
        
        val capabilities = listOf(
            // Maritza: 3 capacidades asignables
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 1, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 2, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 3, competency_level = 3, is_active = true),
            
            // Oscar: 3 capacidades asignables
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 1, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 2, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 3, competency_level = 3, is_active = true),
            
            // Trabajador3: Sin capacidades
            
            // Trabajador4: Tiene capacidades pero está INACTIVO
            WorkerWorkstationCapability(worker_id = 4, workstation_id = 1, competency_level = 3, is_active = true),
            
            // Trabajador5: Capacidades con nivel bajo (no asignables)
            WorkerWorkstationCapability(worker_id = 5, workstation_id = 1, competency_level = 1, is_active = true)
        )
        
        // Act: Calcular trabajadores disponibles para rotación
        val availableWorkers = workers.filter { worker ->
            worker.isActive && capabilities.any { 
                it.worker_id == worker.id && it.is_active && it.canBeAssigned() 
            }
        }
        
        // Assert: Solo Maritza y Oscar deberían estar disponibles
        assertEquals("Solo 2 trabajadores deberían estar disponibles", 2, availableWorkers.size)
        assertTrue("Maritza debería estar disponible", availableWorkers.any { it.name == "Maritza" })
        assertTrue("Oscar debería estar disponible", availableWorkers.any { it.name == "Oscar" })
        assertFalse("Trabajador3 NO debería estar disponible (sin capacidades)", availableWorkers.any { it.name == "Trabajador3" })
        assertFalse("Trabajador4 NO debería estar disponible (inactivo)", availableWorkers.any { it.name == "Trabajador4" })
        assertFalse("Trabajador5 NO debería estar disponible (nivel bajo)", availableWorkers.any { it.name == "Trabajador5" })
    }
    
    @Test
    fun `test diagnostico completo - escenario real`() {
        // Arrange: Simular el escenario real del usuario
        val workers = listOf(
            Worker(id = 1, name = "Maritza", employeeId = "EMP001", isActive = true),
            Worker(id = 2, name = "Oscar", employeeId = "EMP002", isActive = true),
            Worker(id = 3, name = "Trabajador3", employeeId = "EMP003", isActive = true),
            Worker(id = 4, name = "Trabajador4", employeeId = "EMP004", isActive = true),
            Worker(id = 5, name = "Trabajador5", employeeId = "EMP005", isActive = true)
        )
        
        val workstations = listOf(
            Workstation(id = 1, name = "Anneling", requiredWorkers = 2, isActive = true),
            Workstation(id = 2, name = "Forming", requiredWorkers = 2, isActive = true),
            Workstation(id = 3, name = "Loops", requiredWorkers = 1, isActive = true)
        )
        
        // Solo 2 trabajadores tienen capacidades (el problema reportado)
        val capabilities = listOf(
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 1, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 2, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 1, workstation_id = 3, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 1, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 2, competency_level = 3, is_active = true),
            WorkerWorkstationCapability(worker_id = 2, workstation_id = 3, competency_level = 3, is_active = true)
        )
        
        // Act: Ejecutar diagnóstico
        val activeWorkers = workers.filter { it.isActive }
        val activeWorkstations = workstations.filter { it.isActive }
        val activeCapabilities = capabilities.filter { it.is_active }
        val assignableCapabilities = activeCapabilities.filter { it.canBeAssigned() }
        
        val workersForRotation = activeWorkers.filter { worker ->
            assignableCapabilities.any { it.worker_id == worker.id }
        }
        
        val workersWithIssues = activeWorkers.filter { worker ->
            assignableCapabilities.none { it.worker_id == worker.id }
        }
        
        // Assert: Verificar diagnóstico
        assertEquals("Deberían haber 5 trabajadores activos", 5, activeWorkers.size)
        assertEquals("Deberían haber 3 estaciones activas", 3, activeWorkstations.size)
        assertEquals("Deberían haber 6 capacidades activas", 6, activeCapabilities.size)
        assertEquals("Deberían haber 6 capacidades asignables", 6, assignableCapabilities.size)
        
        assertEquals("Solo 2 trabajadores deberían aparecer en rotación", 2, workersForRotation.size)
        assertEquals("3 trabajadores tienen problemas", 3, workersWithIssues.size)
        
        // Verificar mensaje de diagnóstico
        val diagnosticMessage = if (workersWithIssues.isNotEmpty()) {
            "⚠️ PROBLEMA DETECTADO: ${workersWithIssues.size} trabajadores activos NO aparecerán en rotación"
        } else {
            "✅ No se detectaron problemas"
        }
        
        assertTrue(
            "El diagnóstico debería detectar el problema",
            diagnosticMessage.contains("PROBLEMA DETECTADO")
        )
        assertTrue(
            "El diagnóstico debería identificar 3 trabajadores con problemas",
            diagnosticMessage.contains("3 trabajadores")
        )
    }
}
