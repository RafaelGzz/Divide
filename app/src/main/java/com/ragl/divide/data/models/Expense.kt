package com.ragl.divide.data.models

import java.util.Date

interface IExpense{
    val id: String
    val title: String
    val category: Category
    val amount: Double
    val notes: String
    val addedDate: Long
}

data class Expense(
    override val id: String = "",
    override val title: String = "",
    override val category: Category = Category.GENERAL,
    override val amount: Double = 0.0,
    override val notes: String = "",
    override val addedDate: Long = Date().time,
    val amountPaid: Double = 0.0,
    val numberOfPayments: Int = 1,
    val reminders: Boolean = false,
    val frequency: Frequency = Frequency.DAILY,
    val startingDate: Long = Date().time,
    val payments: Map<String, Payment> = emptyMap(),
    val paid: Boolean = false
): IExpense

data class GroupExpense(
    override val id: String = "",
    override val title: String = "",
    override val category: Category = Category.GENERAL,
    override val amount: Double = 0.0,
    override val notes: String = "",
    override val addedDate: Long = Date().time,
    val method: Method = Method.EQUALLY,
    val paidBy: Map<String, String> = emptyMap(),
    val debtors: Map<String, String> = emptyMap(),
): IExpense