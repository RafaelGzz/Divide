package com.ragl.divide.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category {
    GENERAL,
    ELECTRONICS,
}

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.GENERAL -> Icons.Filled.AttachMoney
        Category.ELECTRONICS -> Icons.Filled.ElectricBolt
    }
}