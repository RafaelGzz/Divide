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
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.home.HomeScreen
import com.ragl.divide.ui.screens.signIn.LogInScreen
import com.ragl.divide.ui.screens.signIn.SignInViewModel
import com.ragl.divide.ui.screens.addFriends.AddFriendsScreen
import com.ragl.divide.ui.screens.expense.ExpenseScreen
import com.ragl.divide.ui.screens.expenseDetails.ExpenseDetailsScreen
import com.ragl.divide.ui.screens.group.GroupScreen
import com.ragl.divide.ui.screens.groupDetails.GroupDetailsScreen
import com.ragl.divide.ui.screens.groupExpense.GroupExpenseScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DivideApp(
    splashScreen: SplashScreen,
    signInViewModel: SignInViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val isLoading by signInViewModel.isLoading.collectAsState()
    splashScreen.setKeepOnScreenCondition { isLoading }
    val context = LocalContext.current
    val startDestination by signInViewModel.startDestination.collectAsState()
    val user by userViewModel.user.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<Screen.Splash> {
            Scaffold {
                Box {}
            }
        }
        composable<Screen.Login> {
            LogInScreen(
                onGoogleButtonClick = {
                    signInViewModel.signInWithGoogle(
                        context,
                        {
                            userViewModel.getUserData()
                            navController.navTo(Screen.Home, true)
                        },
                        { showToast(context, it) }
                    )
                },
                onLoginButtonClick = { email, password ->
                    signInViewModel.signInWithEmailAndPassword(email, password,
                        {
                            userViewModel.getUserData()
                            navController.navTo(Screen.Home, true)
                        },
                        { showToast(context, it) })
                },
                onSignUpButtonClick = { email, password, name ->
                    signInViewModel.signUpWithEmailAndPassword(email, password, name,
                        {
                            userViewModel.getUserData()
                            navController.navTo(Screen.Home, true)
                        },
                        { showToast(context, it) })
                },
                isLoading = isLoading
            )
        }
        composable<Screen.Home> {
            HomeScreen(
                vm = userViewModel,
                onAddExpenseClick = { navController.navTo(Screen.Expense()) },
                onAddGroupClick = { navController.navTo(Screen.Group()) },
                onAddFriendsClick = { navController.navTo(Screen.AddFriends) },
                onExpenseClick = { expenseId -> navController.navTo(Screen.ExpenseDetails(expenseId = expenseId)) },
                onGroupClick = { groupId -> navController.navTo(Screen.GroupDetails(groupId = groupId)) },
                onSignOut = { navController.navTo(Screen.Login, true) }
            )
        }
        composable<Screen.Expense> {
            val args: Screen.Expense = it.toRoute()
            val isUpdate = args.expenseId.isNotEmpty()
            ExpenseScreen(
                onBackClick = { navController.navigateUp() },
                expense = if(isUpdate) user.expenses[args.expenseId]!! else Expense(),
                isUpdate = isUpdate,
                onSaveExpense = {
                    if (isUpdate) {
                        userViewModel.getUserData()
                        navController.navTo(
                            Screen.Home,
                            pop = true,
                            incl = true
                        )
                    } else {
                        navController.navTo(
                            Screen.ExpenseDetails(expenseId = args.expenseId),
                            pop = true,
                            incl = true
                        )
                    }
                },
            )
        }
        composable<Screen.ExpenseDetails> {
            val args: Screen.ExpenseDetails = it.toRoute()
            ExpenseDetailsScreen(
                expense = user.expenses[args.expenseId]!!,
                editExpense = { id -> navController.navTo(Screen.Expense(expenseId = id)) },
                onBackClick = { navController.navigateUp() },
                onDeleteExpense = {
                    userViewModel.getUserData()
                    navController.navTo(
                        Screen.Home,
                        pop = true,
                        incl = true
                    )
                },
                onPaidExpense = {
                    userViewModel.getUserData()
                    navController.navTo(
                        Screen.Home,
                        pop = true,
                        incl = true
                    )
                }
            )
        }
        composable<Screen.Group> {
            val args: Screen.Group = it.toRoute()
            val isUpdate = args.groupId.isNotEmpty()
            GroupScreen(
                friends = user.friends.values.toList(),
                group = if(isUpdate) user.groups[args.groupId] ?: Group() else Group(),
                isUpdate = isUpdate,
                onBackClick = { navController.navigateUp() },
                onDeleteGroup = {
                    userViewModel.getUserData()
                    navController.navTo(Screen.Home, true)
                },
                onAddGroup = {
                    userViewModel.getUserData()
                    navController.navTo(Screen.Home, true)
                }
            )
        }
        composable<Screen.GroupDetails> {
            val args: Screen.GroupDetails = it.toRoute()
            GroupDetailsScreen(
                group = user.groups[args.groupId]!!,
                editGroup = { id -> navController.navTo(Screen.Group(groupId = id)) },
                onBackClick = { navController.navigateUp() },
                onAddExpenseClick = { navController.navTo(Screen.GroupExpense) }
            )
        }
        composable<Screen.GroupExpense> {
            GroupExpenseScreen(
                onBackClick = { navController.navigateUp() },
                onSaveExpense = {}
            )
        }
        composable<Screen.AddFriends> {
            AddFriendsScreen(
                friends = user.friends.values.toList(),
                onBackClick = {
                    userViewModel.getUserData()
                    navController.navigateUp()
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
