package com.ragl.divide.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragl.divide.ui.screens.HomeScreen
import com.ragl.divide.ui.screens.LoginScreen
import com.ragl.divide.ui.screens.UserViewModel

@Composable
fun DivideApp(
    userViewModel: UserViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    Scaffold { padding ->
        val context = LocalContext.current
        val startDestination = if (userViewModel.user != null) "Home" else "Login"
        NavHost(navController = navController, startDestination = startDestination) {
            composable("Login") {
                LoginScreen(
                    onGoogleButtonClick = {
                        userViewModel.signInWithGoogle(
                            context,
                            { navigateTo(navController, "Home", "Login") },
                            { showToast(context, it) }
                        )
                    },
                    onLoginButtonClick = { email, password ->
                        userViewModel.signInWithEmailAndPassword(email, password,
                            { navigateTo(navController, "Home", "Login") },
                            { showToast(context, it) })
                    },
                    onSignUpButtonClick = { email, password, name ->
                        userViewModel.signUpWithEmailAndPassword(email, password, name,
                            { navigateTo(navController, "Home", "Login") },
                            { showToast(context, it) })
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            composable("Home") {
                HomeScreen(onLogOut = { navigateTo(navController, "Login", "Home") })
            }
        }
    }
}

fun navigateTo(navController: NavHostController, route: String, popUpTo: String) =
    navController.navigate(route) {
        popUpTo(popUpTo) {
            inclusive = true
        }
    }


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}