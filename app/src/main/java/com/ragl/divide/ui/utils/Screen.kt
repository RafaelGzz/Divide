package com.ragl.divide.ui.utils

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {

    @Serializable
    data object Splash : Screen("Splash")
    @Serializable
    data object Login : Screen("Login")
    @Serializable
    data object Home : Screen("Home")
    @Serializable
    data class Expense(val expenseId: String = "") : Screen("Expense")
    @Serializable
    data class ExpenseDetails(val expenseId: String = "") : Screen("ExpenseDetails")
    @Serializable
    data class Group(val groupId: String = "") : Screen("Group")
    @Serializable
    data class GroupDetails(val groupId: String = "") : Screen("GroupDetails")
    @Serializable
    data class GroupExpense(val groupId: String = "", val expenseId: String = "") : Screen("GroupExpense")
    @Serializable
    data class GroupExpenseDetails(val groupId: String = "", val expenseId: String = "") : Screen("GroupExpenseDetails")
    @Serializable
    data object AddFriends: Screen("AddFriends")

    companion object {
        fun stringToScreen(route: String): Screen = when (route) {
            Login.route -> Login
            Home.route -> Home
            else -> Login
        }
    }
}