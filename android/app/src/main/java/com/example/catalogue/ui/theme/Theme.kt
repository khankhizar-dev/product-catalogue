package com.example.catalogue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightCatalogColors = lightColorScheme(
    primary = Brand,
    onPrimary = BrandOn,
    primaryContainer = BrandContainer,
    onPrimaryContainer = BrandOnContainer,
    background = SurfaceColor,
    surface = SurfaceColor,
    onBackground = OnSurface,
    onSurface = OnSurface,
    error = ErrorRed,
)

private val DarkCatalogColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    background = DarkSurfaceColor,
    surface = DarkSurfaceColor,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    error = DarkErrorRed,
)

@Composable
fun CatalogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkCatalogColors else LightCatalogColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CatalogTypography,
        content = content,
    )
}
