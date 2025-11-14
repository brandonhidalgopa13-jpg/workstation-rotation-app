package com.workstation.rotation

actual fun getPlatform(): Platform = Platform.Desktop(
    os = System.getProperty("os.name")
)
