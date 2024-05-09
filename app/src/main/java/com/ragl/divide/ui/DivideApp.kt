package com.ragl.divide.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragl.divide.ui.screens.HomeScreen
import com.ragl.divide.ui.screens.LoginScreen

@Composable
fun DivideApp(){
    Scaffold {padding ->
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "Login" ) {
            composable("Login" ){
                LoginScreen(
                    onSuccess = {
                        navController.navigate("Home"){
                            popUpTo("Login"){
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            composable("Home" ){
                HomeScreen()
            }
        }
    }
}