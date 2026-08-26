package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Instagram & Story Accents
val IgPurple = Color(0xFF833AB4)
val IgMagenta = Color(0xFFC13584)
val IgPink = Color(0xFFE1306C)
val IgRed = Color(0xFFFD1D1D)
val IgOrange = Color(0xFFF56040)
val IgYellow = Color(0xFFFCAF45)
val IgLikeRed = Color(0xFFED4956)

// Professional Polish Theme Colors (Light)
val PolishPrimary = Color(0xFF005AC1)
val PolishOnPrimary = Color(0xFFFFFFFF)
val PolishPrimaryContainer = Color(0xFFD3E4FF)
val PolishOnPrimaryContainer = Color(0xFF001D35)
val PolishSecondary = Color(0xFF535F70)
val PolishSecondaryContainer = Color(0xFFD7E3F8)
val PolishBackground = Color(0xFFF8F9FF)
val PolishSurface = Color(0xFFFFFFFF)
val PolishSurfaceVariant = Color(0xFFF0F4F9)
val PolishOutline = Color(0xFFDCE2EA)
val PolishOnSurface = Color(0xFF191C20)
val PolishOnSurfaceVariant = Color(0xFF44474E)

// Professional Polish Theme Colors (Dark)
val PolishDarkPrimary = Color(0xFFADC6FF)
val PolishDarkOnPrimary = Color(0xFF002E69)
val PolishDarkPrimaryContainer = Color(0xFF004494)
val PolishDarkOnPrimaryContainer = Color(0xFFD3E4FF)
val PolishDarkBackground = Color(0xFF0B131E)
val PolishDarkSurface = Color(0xFF121C2A)
val PolishDarkSurfaceVariant = Color(0xFF1E2A3C)
val PolishDarkOutline = Color(0xFF2F3D52)
val PolishDarkOnSurface = Color(0xFFE1E2EC)
val PolishDarkOnSurfaceVariant = Color(0xFFC4C6D0)

// Story & Reels Gradient
val InstagramStoryGradient = Brush.linearGradient(
    colors = listOf(IgPurple, IgMagenta, IgPink, IgOrange, IgYellow)
)

val InstagramStorySeenGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF8E8E8E), Color(0xFF555555))
)

