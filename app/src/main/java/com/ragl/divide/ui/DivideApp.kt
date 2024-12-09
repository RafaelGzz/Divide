package com.ragl.divide.ui

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.addFriends.AddFriendsScreen
import com.ragl.divide.ui.screens.expense.ExpenseScreen
import com.ragl.divide.ui.screens.expenseDetails.ExpenseDetailsScreen
import com.ragl.divide.ui.screens.group.GroupScreen
import com.ragl.divide.ui.screens.groupDetails.GroupDetailsScreen
import com.ragl.divide.ui.screens.groupExpense.GroupExpenseScreen
import com.ragl.divide.ui.screens.groupExpenseDetails.GroupExpenseDetailsScreen
import com.ragl.divide.ui.screens.groupPaymentDetails.GroupPaymentDetailsScreen
import com.ragl.divide.ui.screens.home.HomeScreen
import com.ragl.divide.ui.screens.signIn.LogInScreen
import com.ragl.divide.ui.screens.signIn.SignInViewModel
import com.ragl.divide.ui.theme.DivideTheme
import com.ragl.divide.ui.utils.Screen

@OptIn(ExperimentalSharedTransitionApi::class)
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
    val user by userViewModel.state.collectAsState()
    val darkMode by userViewModel.isDarkMode.collectAsState()
    DivideTheme(darkMode?.toBoolean() ?: isSystemInDarkTheme()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable<Screen.Splash>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    Scaffold {
                        Box {}
                    }
                }
                composable<Screen.Login>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
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
                composable<Screen.Home>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    HomeScreen(
                        vm = userViewModel,
                        onAddExpenseClick = { navController.navTo(Screen.Expense()) },
                        onAddGroupClick = { navController.navTo(Screen.Group()) },
                        onAddFriendsClick = { navController.navTo(Screen.AddFriends) },
                        onExpenseClick = { expenseId ->
                            navController.navTo(
                                Screen.ExpenseDetails(
                                    expenseId = expenseId
                                )
                            )
                        },
                        onGroupClick = { groupId ->
                            userViewModel.getGroupMembers(user.groups[groupId]!!)
                            navController.navTo(Screen.GroupDetails(groupId = groupId))
                        },
                        onSignOut = {
                            navController.navTo(Screen.Login, true)
                        },
                        isDarkMode = userViewModel.isDarkMode.value,
                        onChangeDarkMode = { darkMode: String? ->
                            userViewModel.isDarkMode.value = darkMode
                        }
                    )
                }
                composable<Screen.Expense>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    val args: Screen.Expense = it.toRoute()
                    val isUpdate = args.expenseId.isNotEmpty()
                    ExpenseScreen(
                        onBackClick = { navController.navigateUp() },
                        expense = if (isUpdate) user.expenses[args.expenseId]!! else Expense(),
                        isUpdate = isUpdate,
                        onSaveExpense = { savedExpense ->
                            userViewModel.saveExpense(savedExpense)
                            navController.navTo(
                                Screen.Home, pop = true, incl = true
//                            Screen.ExpenseDetails(expenseId = savedExpense.id),
//                            pop = true,
//                            incl = true
                            )
                        },
                    )
                }
                composable<Screen.ExpenseDetails>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    val args: Screen.ExpenseDetails = it.toRoute()
                    var expense = user.expenses[args.expenseId]
                    ExpenseDetailsScreen(
                        expense = expense ?: Expense(),
                        editExpense = { id -> navController.navTo(Screen.Expense(expenseId = id)) },
                        onBackClick = {
                            if (navController.previousBackStackEntry?.destination?.route == Screen.Expense().javaClass.canonicalName)
                                navController.navTo(
                                    Screen.Home,
                                    pop = true,
                                    incl = true
                                )
                            else
                                navController.navigateUp()
                        },
                        onPaymentMade = { payment: Payment ->
                            userViewModel.savePayment(expense!!.id, payment)
                        },
                        onDeletePayment = { paymentId ->
                            userViewModel.deletePayment(expense!!.id, paymentId)
                        },
                        onDeleteExpense = {
                            userViewModel.removeExpense(args.expenseId)
                            navController.navTo(
                                Screen.Home,
                                pop = true,
                                incl = true
                            )
                        },
                        onPaidExpense = {
                            expense = Expense()
                            userViewModel.paidExpense(args.expenseId)
                            navController.navTo(
                                Screen.Home,
                                pop = true,
                                incl = true
                            )
                        }
                    )
                }
                composable<Screen.Group>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    val args: Screen.Group = it.toRoute()
                    GroupScreen(
                        friends = user.friends.values.toList(),
                        group = if (args.groupId.isNotEmpty()) user.groups[args.groupId]
                            ?: Group() else Group(),
                        isUpdate = args.groupId.isNotEmpty(),
                        members = user.selectedGroupMembers,
                        user = user.user,
                        onBackClick = { navController.navigateUp() },
                        onDeleteGroup = {
                            userViewModel.removeGroup(args.groupId)
                            navController.navTo(
                                Screen.Home,
                                pop = true,
                                incl = true
                            )
                        },
                        onSaveGroup = { group ->
                            userViewModel.addGroup(group)
                            navController.navigateUp()
                        }
                    )
                }
                composable<Screen.GroupDetails>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    val args: Screen.GroupDetails = it.toRoute()
                    AnimatedVisibility(
                        !userViewModel.isLoadingMembers,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = ExitTransition.None
                    ) {
                        GroupDetailsScreen(
                            group = user.groups[args.groupId]!!,
                            userId = user.user.uuid,
                            members = user.selectedGroupMembers,
                            editGroup = { id -> navController.navTo(Screen.Group(groupId = id)) },
                            onBackClick = { navController.navigateUp() },
                            onAddExpenseClick = { navController.navTo(Screen.GroupExpense(args.groupId)) },
                            onExpenseClick = { expenseId ->
                                navController.navTo(
                                    Screen.GroupExpenseDetails(
                                        groupId = args.groupId,
                                        expenseId = expenseId
                                    )
                                )
                            },
                            onAddPaymentClick = {
                                navController.navTo(
                                    Screen.GroupPaymentDetails(
                                        groupId = args.groupId
                                    )
                                )
                            }
                        )
                    }
                    AnimatedVisibility(
                        userViewModel.isLoadingMembers,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = ExitTransition.None
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    }
                }
                composable<Screen.GroupExpense>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    val args: Screen.GroupExpense = it.toRoute()
                    GroupExpenseScreen(
                        onBackClick = { navController.navigateUp() },
                        group = user.groups[args.groupId]!!,
                        expense = if (args.expenseId.isNotEmpty()) user.groups[args.groupId]!!.expenses[args.expenseId]!! else GroupExpense(),
                        userId = user.user.uuid,
                        members = user.selectedGroupMembers,
                        onSaveExpense = { groupExpense, oldExpense ->
                            if (args.expenseId.isEmpty()) {
                                userViewModel.saveGroupExpense(args.groupId, groupExpense)
                            } else {
                                userViewModel.updateGroupExpense(
                                    args.groupId,
                                    groupExpense,
                                    oldExpense
                                )
                            }
                            navController.navigateUp()
                        }
                    )
                }
                composable<Screen.GroupExpenseDetails>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    val args: Screen.GroupExpenseDetails = it.toRoute()
                    GroupExpenseDetailsScreen(
                        groupExpense = user.groups[args.groupId]!!.expenses[args.expenseId]
                            ?: GroupExpense(),
                        groupId = args.groupId,
                        members = user.selectedGroupMembers,
                        onBackClick = { navController.navigateUp() },
                        onEditClick = { expenseId ->
                            navController.navTo(
                                Screen.GroupExpense(
                                    groupId = args.groupId,
                                    expenseId = expenseId
                                )
                            )
                        },
                        onDeleteExpense = { groupExpense ->
                            userViewModel.removeGroupExpense(args.groupId, groupExpense)
                            navController.navigateUp()
                        }
                    )
                }
                composable<Screen.GroupPaymentDetails>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    val args: Screen.GroupPaymentDetails = it.toRoute()
                    GroupPaymentDetailsScreen(
                        payment = user.groups[args.groupId]!!.payments[args.paymentId] ?: Payment(),
                        group = user.groups[args.groupId]!!,
                        groupUser = user.groups[args.groupId]!!.users[user.user.uuid]!!,
                        members = user.selectedGroupMembers,
                        onBackClick = { navController.navigateUp() }
                    )
                }
                composable<Screen.AddFriends>(
                    enterTransition = {
                        fadeIn(animationSpec = tween(500))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(300))
                    }
                ) {
                    AddFriendsScreen(
                        friends = user.friends.values.toList(),
                        onFriendAdded = {
                            userViewModel.addFriend(it)
                        },
                        onBackClick = {
                            navController.navigateUp()
                        }
                    )
                }
            }
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
