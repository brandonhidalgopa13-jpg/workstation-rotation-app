package com.workstation.rotation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
