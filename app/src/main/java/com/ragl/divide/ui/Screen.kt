package com.ragl.divide.ui

sealed class Screen(val route: String){
    data object Splash: Screen("Splash")
    data object Home: Screen("Home")
    data object Login: Screen("Login")

}