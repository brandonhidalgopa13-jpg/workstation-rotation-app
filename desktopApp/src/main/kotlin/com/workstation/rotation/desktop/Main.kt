package com.workstation.rotation.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.workstation.rotation.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Rotation"
    ) {
        App()
    }
}
