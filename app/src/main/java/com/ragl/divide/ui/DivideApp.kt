package com.ragl.divide.ui

import android.content.Context
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ragl.divide.ui.screens.home.HomeScreen
import com.ragl.divide.ui.screens.login.LoginScreen
import com.ragl.divide.ui.screens.UserViewModel

@Composable
fun DivideApp(
    splashScreen: SplashScreen,
    userViewModel: UserViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val isLoading by userViewModel.isLoading.collectAsState()
    splashScreen.setKeepOnScreenCondition{isLoading}
    val context = LocalContext.current
    val startDestination by userViewModel.startDestination.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route){
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onGoogleButtonClick = {
                    userViewModel.signInWithGoogle(
                        context,
                        { navigateTo(navController, Screen.Home.route, Screen.Login.route) },
                        { showToast(context, it) }
                    )
                },
                onLoginButtonClick = { email, password ->
                    userViewModel.signInWithEmailAndPassword(email, password,
                        { navigateTo(navController, Screen.Home.route, Screen.Login.route) },
                        { showToast(context, it) })
                },
                onSignUpButtonClick = { email, password, name ->
                    userViewModel.signUpWithEmailAndPassword(email, password, name,
                        { navigateTo(navController, Screen.Home.route, Screen.Login.route) },
                        { showToast(context, it) })
                },
                isLoading = isLoading
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                user = userViewModel.user!!,
                onSignOut = { navigateTo(navController, Screen.Login.route, Screen.Home.route) }
            )
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