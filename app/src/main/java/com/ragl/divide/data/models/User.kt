package com.ragl.divide.data.models

data class User(
    val uuid: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val groups: Map<String, String> = emptyMap(),
    val expenses: Map<String, Expense> = emptyMap()
)