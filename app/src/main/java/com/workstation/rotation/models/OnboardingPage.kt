package com.workstation.rotation.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * Modelo de datos para las páginas del onboarding
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    @ColorRes val backgroundColor: Int
)