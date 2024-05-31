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
    data object AddGroup : Screen("AddGroup")
    @Serializable
    data object AddExpense : Screen("AddExpense")
    @Serializable
    data class ExpenseDetails(
        val expenseId: String = ""
    ) : Screen("ExpenseDetails")

    companion object{
        fun stringToScreen(route: String) : Screen = when(route){
            Login.route -> Login
            Home.route -> Home
            else -> Login
        }
    }
}