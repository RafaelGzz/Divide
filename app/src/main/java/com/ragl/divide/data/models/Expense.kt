package com.ragl.divide.data.models

import java.util.Date

data class Expense(
    val id: String = "",
    val title: String = "",
    val category: Category = Category.GENERAL,
    val amount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val numberOfPayments: Int = 1,
    val notes: String = "",
    val reminders: Boolean = false,
    val frequency: Frequency = Frequency.DAILY,
    val addedDate: Long = Date().time,
    val startingDate: Long = Date().time,
    val payments: Map<String, Payment> = emptyMap(),
    val paid: Boolean = false,
//    val method: Method = Method.EQUALLY,
//    val paidBy: List<String> = emptyList(),
//    val debtors: List<String> = emptyList(),
)