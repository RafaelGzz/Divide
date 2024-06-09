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
import com.ragl.divide.ui.screens.group.GroupScreen
import com.ragl.divide.ui.screens.groupDetails.GroupDetailsScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DivideApp(
    splashScreen: SplashScreen,
    userViewModel: UserViewModel = hiltViewModel(),
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
                        { navController.navTo(Screen.Home(), true) },
                        { showToast(context, it) }
                    )
                },
                onLoginButtonClick = { email, password ->
                    userViewModel.signInWithEmailAndPassword(email, password,
                        { navController.navTo(Screen.Home(), true) },
                        { showToast(context, it) })
                },
                onSignUpButtonClick = { email, password, name ->
                    userViewModel.signUpWithEmailAndPassword(email, password, name,
                        { navController.navTo(Screen.Home(), true) },
                        { showToast(context, it) })
                },
                isLoading = isLoading
            )
        }
        composable<Screen.Home> {
            val args: Screen.Home = it.toRoute()
            HomeScreen(
                onAddExpenseClick = {
                    navController.navTo(
                        Screen.AddExpense()
                    )
                },
                onAddGroupClick = {
                    navController.navTo(
                        Screen.AddGroup()
                    )
                },
                onExpenseClick = { expenseId ->
                    navController.navTo(Screen.ExpenseDetails(expenseId = expenseId))
                },
                onGroupClick = { groupId ->
                    navController.navTo(Screen.GroupDetails(groupId = groupId))
                },
                onSignOut = { navController.navTo(Screen.Login, true) },
                paidExpense = args.paidExpense
            )
        }
        composable<Screen.AddExpense> {
            val args: Screen.AddExpense = it.toRoute()
            ExpenseScreen(
                onBackClick = { navController.popBackStack() },
                expenseId = args.expenseId,
                onAddExpense = {
                    if (args.expenseId.isEmpty()) navController.navTo(
                        Screen.Home(),
                        true
                    ) else navController.navTo(
                        Screen.ExpenseDetails(expenseId = args.expenseId),
                        pop = true,
                        incl = true
                    )
                },
            )
        }
        composable<Screen.ExpenseDetails> {
            val args: Screen.ExpenseDetails = it.toRoute()
            ExpenseDetailsScreen(
                expenseId = args.expenseId,
                editExpense = { id ->
                    navController.navTo(Screen.AddExpense(expenseId = id))
                },
                onBackClick = { navController.popBackStack() },
                onDeleteExpense = {
                    navController.navTo(Screen.Home(), true)
                },
                onPaidExpense = {
                    navController.navTo(Screen.Home(true), true)
                }
            )
        }
        composable<Screen.AddGroup> {
            val args: Screen.AddGroup = it.toRoute()
            GroupScreen(
                groupId = args.groupId,
                onBackClick = { navController.popBackStack() },
                onAddGroup = {
                    navController.navTo(Screen.Home(), true)
                }
            )
        }
        composable<Screen.GroupDetails> {
            val args: Screen.GroupDetails = it.toRoute()
            GroupDetailsScreen(
                groupId = args.groupId,
                editGroup = { id ->
                    navController.navTo(Screen.AddGroup(groupId = id))
                },
                onBackClick = { navController.popBackStack() },
                onAddExpense = {
                    //navController.navTo(Screen.AddExpense(groupId = it))
                }
            )
        }
    }
}

fun NavHostController.navTo(route: Screen, pop: Boolean = false, incl: Boolean = false) =
    navigate(route) {
        if (pop) popUpTo(route) {
            inclusive = incl
        }
    }


fun showToast(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
