package com.example.catalogue.ui.theme

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.dp

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val gridMinCellWidth = 144.dp
}

val CatalogGridCells: GridCells = GridCells.Adaptive(minSize = Spacing.gridMinCellWidth)
