package com.ragl.divide.ui.screens

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private var auth: FirebaseAuth
) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var emailError by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var passwordError by mutableStateOf("")
        private set

    fun updateEmail(email: String) {
        this.email = email.trim()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
    }

    fun tryLogin(onSuccessfulLogin: () -> Unit, onFailedLogin: (String) -> Unit) {
        validateEmail()
        validatePassword()

        if (validateEmail() && validatePassword()) {
            viewModelScope.launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) onSuccessfulLogin()
                    }.addOnFailureListener {
                        onFailedLogin(it.message.orEmpty())
                    }
                } catch (e: Exception) {
                    onFailedLogin(e.message.orEmpty())
                }
            }
        }
    }
    private fun validateEmail(): Boolean {
        if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = ""
            return true
        } else {
            emailError = "Email is not valid"
            return false
        }
    }

    private fun validatePassword(): Boolean {
        if (password.isNotBlank()) {
            passwordError = ""
            return true
        } else {
            passwordError = "Password is required"
            return false
        }
    }
}