package com.ragl.divide.data.models

data class Group(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val expenses: Map<String, Expense> = emptyMap(),
    val users: MutableMap<String, String> = mutableMapOf(),
)
