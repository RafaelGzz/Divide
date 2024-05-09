package com.ragl.divide.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.ragl.divide.ui.screens.HomeScreen
import com.ragl.divide.ui.screens.LoginScreen

@Composable
fun DivideApp(auth: FirebaseAuth = Firebase.auth) {
    Scaffold { padding ->
        val navController = rememberNavController()
        val startDestination = if (auth.currentUser != null) "Home" else "Login"
        NavHost(navController = navController, startDestination = startDestination) {
            composable("Login") {
                LoginScreen(
                    onSuccess = {
                        navController.navigate("Home") {
                            popUpTo("Login") {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            composable("Home") {
                HomeScreen(onLogOut = {
                    navController.navigate("Login") {
                        popUpTo("Home") {
                            inclusive = true
                        }
                    }
                })
            }
        }
    }
}