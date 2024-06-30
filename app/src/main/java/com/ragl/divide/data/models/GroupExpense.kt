package com.ragl.divide.data.models

import java.util.Date

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