package com.workstation.rotation

import android.os.Build

actual fun getPlatform(): Platform = Platform.Android(
    version = Build.VERSION.SDK_INT
)

sealed class Platform {
    data class Android(val version: Int) : Platform()
    data class IOS(val version: String) : Platform()
    data class Desktop(val os: String) : Platform()
}
