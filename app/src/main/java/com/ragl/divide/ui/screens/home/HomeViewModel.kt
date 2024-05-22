package com.ragl.divide.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.PreferencesRepository
import com.ragl.divide.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val expenses: List<String> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private var userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val expenses = emptyList<String>() //userRepository.getExpenses()
                _state.update {
                    it.copy(
                        expenses = expenses
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", e.message.toString())
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                if (userRepository.getFirebaseUser() == null) {
                    preferencesRepository.saveStartDestination("")
                    onSignOut()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", e.message.toString())
            }
        }
    }
}