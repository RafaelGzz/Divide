package com.ragl.divide.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragl.divide.ui.screens.LoginScreen

@Composable
fun DivideApp(){
    Scaffold {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "Login" ) {
            composable("Login" ){
                LoginScreen()
            }
        }
    }
}