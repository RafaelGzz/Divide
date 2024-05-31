package com.ragl.divide.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val expenses: Map<String, Expense> = emptyMap(),
    val groups: List<Group> = emptyList(),
    val user: User = User()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private var userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        getUserData()
    }

    private fun getUserData(){
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            try {
                val user = userRepository.getDatabaseUser()!!
                val groups = groupRepository.getGroups(user.groups)
                val expenses = userRepository.getExpenses()
                _state.update {
                    it.copy(
                        expenses = expenses,
                        groups = groups,
                        user = user,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HomeViewModel", e.message.toString())
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    preferencesRepository.saveStartDestination(Screen.Login.route)
                    onSignOut()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", e.message.toString())
            }
        }
    }
}