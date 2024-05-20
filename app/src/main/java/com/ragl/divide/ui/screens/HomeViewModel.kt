package com.ragl.divide.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ragl.divide.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private var userRepository: UserRepository
) : ViewModel() {
    fun logout(onSignOut: () -> Unit) {
        try {
            userRepository.signOut()
            if (userRepository.getFirebaseUser() == null) onSignOut()
        } catch (e: Exception) {
            Log.e("HomeViewModel", e.message.toString())
        }
    }
}