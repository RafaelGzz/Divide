package com.ragl.divide.data.models

data class GroupUser(
    val id: String = "",
    val expenses: Map<String, Double> = emptyMap(),
    val payments: Map<String, Double> = emptyMap(),
    val owed: Double = 0.0,
    val debt: Double = 0.0,
)