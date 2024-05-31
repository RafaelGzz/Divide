package com.ragl.divide.ui

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
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
import androidx.navigation.toRoute
import com.ragl.divide.ui.screens.home.HomeScreen
import com.ragl.divide.ui.screens.login.LoginScreen
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.expense.ExpenseScreen
import com.ragl.divide.ui.screens.expenseDetails.ExpenseDetailsScreen
import com.ragl.divide.ui.screens.expenseDetails.ExpenseDetailsViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DivideApp(
    splashScreen: SplashScreen,
    userViewModel: UserViewModel = hiltViewModel(),
    expenseDetailsViewModel: ExpenseDetailsViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val isLoading by userViewModel.isLoading.collectAsState()
    splashScreen.setKeepOnScreenCondition { isLoading }
    val context = LocalContext.current
    val startDestination by userViewModel.startDestination.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<Screen.Splash> {
            Scaffold {
                Box {}
            }
        }
        composable<Screen.Login> {
            LoginScreen(
                onGoogleButtonClick = {
                    userViewModel.signInWithGoogle(
                        context,
                        { navController.navTo(Screen.Home, true) },
                        { showToast(context, it) }
                    )
                },
                onLoginButtonClick = { email, password ->
                    userViewModel.signInWithEmailAndPassword(email, password,
                        { navController.navTo(Screen.Home, true) },
                        { showToast(context, it) })
                },
                onSignUpButtonClick = { email, password, name ->
                    userViewModel.signUpWithEmailAndPassword(email, password, name,
                        { navController.navTo(Screen.Home, true) },
                        { showToast(context, it) })
                },
                isLoading = isLoading
            )
        }
        composable<Screen.Home> {
            HomeScreen(
                onAddExpenseClick = {
                    navController.navTo(
                        Screen.AddExpense
                    )
                },
                onAddGroupClick = {
                    navController.navTo(
                        Screen.AddGroup
                    )
                },
                onExpenseClick = {
                    navController.navTo(Screen.ExpenseDetails(expenseId = it))
                },
                onSignOut = { navController.navTo(Screen.Login, true) },
            )
        }
        composable<Screen.AddExpense> {
            ExpenseScreen(
                onBackClick = {navController.popBackStack() },
                onAddExpense = { navController.navTo(Screen.Home, true) },
            )
        }
        composable<Screen.ExpenseDetails> {
            val args: Screen.ExpenseDetails = it.toRoute()
            ExpenseDetailsScreen(
                expenseState = expenseDetailsViewModel.expense,
                isLoadingState = expenseDetailsViewModel.isLoading,
                deleteExpense = { id, onSuccess, onFailure ->
                    expenseDetailsViewModel.deleteExpense(id, onSuccess, onFailure)
                },
                deletePayment = { id, onFailure ->
                    expenseDetailsViewModel.deletePayment(id, onFailure)
                },
                addPayment = { amount, onFailure ->
                    expenseDetailsViewModel.addPayment(amount, onFailure)
                },
                loadExpense = {
                    expenseDetailsViewModel.setExpense(args.expenseId)
                },
                onBackClick = { navController.popBackStack() },
                onDeleteExpense = {
                    navController.navTo(Screen.Home, true)
                }
            )
        }
    }
}
fun NavHostController.navTo(route: Screen, pop: Boolean = false) = navigate(route) {
    if (pop) popUpTo(route) {
        inclusive = false
    }
}


fun showToast(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
