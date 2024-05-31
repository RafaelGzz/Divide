package com.ragl.divide.data.models

data class User(
    val uuid: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val groups: List<String> = emptyList(),
    val expenses: Map<String, Expense> = emptyMap()
)