import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
}

compose.desktop {
    application {
        mainClass = "com.workstation.rotation.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WorkstationRotation"
            packageVersion = "5.0.0"
            
            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                menuGroup = "Workstation Rotation"
                upgradeUuid = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
            }
            
            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
            }
            
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
        }
    }
}
