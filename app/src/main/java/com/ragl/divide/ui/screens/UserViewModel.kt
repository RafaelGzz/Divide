package com.ragl.divide.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserState(
    val isLoading: Boolean = false,
    val expenses: Map<String, Expense> = emptyMap(),
    val groups: Map<String, Group> = emptyMap(),
    val friends: Map<String, User> = emptyMap(),
    val selectedGroupMembers: List<User> = emptyList(),
    val user: User = User()
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private var userRepository: UserRepository,
    private var friendsRepository: FriendsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _user = MutableStateFlow(UserState())
    val user = _user.asStateFlow()

    private var selectedGroupId by mutableStateOf("")

    var isLoadingMembers by mutableStateOf(false)
        private set

    init {
        if (userRepository.getFirebaseUser() != null)
            getUserData()
    }

    fun getUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            _user.update {
                it.copy(isLoading = true)
            }
            try {
                val user = userRepository.getUser(userRepository.getFirebaseUser()!!.uid)
                val groups = groupRepository.getGroups(user.groups)
                val expenses = userRepository.getExpenses()
                val friends = friendsRepository.getFriends(user.friends)
                _user.update {
                    it.copy(
                        expenses = expenses,
                        groups = groups,
                        friends = friends,
                        user = user
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HomeViewModel", e.message.toString())
            }
            _user.update {
                it.copy(isLoading = false)
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            try {
                _user.update {
                    it.copy(isLoading = true)
                }
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    preferencesRepository.saveStartDestination(Screen.Login.route)
                    onSignOut()
                    _user.update {
                        UserState(isLoading = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", e.message.toString())
            }
        }
    }

    fun removeExpense(expenseId: String) {
        _user.update {
            it.copy(expenses = it.expenses - expenseId)
        }
    }

    fun paidExpense(expenseId: String) {
        _user.update {
            it.copy(expenses = it.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(paid = true)
                } else {
                    expense.value
                }
            })
        }
    }

    fun removeGroup(groupId: String) {
        _user.update {
            it.copy(groups = it.groups - groupId)
        }
    }

    fun addGroup(group: Group) {
        _user.update {
            it.copy(groups = it.groups + (group.id to group))
        }
    }

    fun saveExpense(expense: Expense) {
        _user.update {
            it.copy(expenses = it.expenses + (expense.id to expense))
        }
    }

    fun addFriend(friend: User) {
        _user.update {
            it.copy(friends = it.friends + (friend.uuid to friend))
        }
    }

    fun savePayment(expenseId: String, payment: Payment) {
        _user.update {
            it.copy(expenses = it.expenses.mapValues { expense ->
                if (expense.key == expenseId) {
                    expense.value.copy(payments = expense.value.payments + (payment.id to payment))
                } else {
                    expense.value
                }
            })
        }
    }

    fun getGroupMembers(group: Group) {
        if (selectedGroupId != group.id)
            viewModelScope.launch {
                selectedGroupId = group.id
                isLoadingMembers = true
                val users = groupRepository.getUsers(group.users.values.toList())
                _user.update {
                    it.copy(selectedGroupMembers = users)
                }
                isLoadingMembers = false
            }
    }

    fun saveGroupExpense(groupId: String, expense: GroupExpense) {
        _user.update {
            it.copy(
                groups = it.groups.mapValues { group ->
                    if (group.key == groupId) {
                        group.value.copy(expenses = group.value.expenses + (expense.id to expense))
                    } else {
                        group.value
                    }
                }
            )
        }
    }
}
