package com.ragl.divide.ui

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
    data class AddGroup(val groupId: String = "") : Screen("AddGroup")

    @Serializable
    data class GroupDetails(val groupId: String = "") : Screen("GroupDetails")

    @Serializable
    data class AddExpense(val expenseId: String = "") : Screen("AddExpense")

    @Serializable
    data class ExpenseDetails(val expenseId: String = "") : Screen("ExpenseDetails")
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