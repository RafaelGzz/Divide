package com.ragl.divide.data.models

data class GroupUser(
    val id: String = "",
    val expenses: Map<String, Double> = emptyMap(),
    val payments: Map<String, Double> = emptyMap(),
    var owed: Double = 0.0,
    var debt: Double = 0.0,
)