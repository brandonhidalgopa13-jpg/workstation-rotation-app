package com.workstation.rotation

import platform.UIKit.UIDevice

actual fun getPlatform(): Platform = Platform.IOS(
    version = UIDevice.currentDevice.systemVersion
)
