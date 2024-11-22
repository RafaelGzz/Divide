package com.ragl.divide.data.models
import java.util.Date
data class Payment(
    val id: String = "",
    val amount: Double = 0.0,
    val date: Long = Date().time,
    val issuer: String = "",
    val receiver: String = "",
    val expensesDeducted: Map<String, Double> = emptyMap()
)