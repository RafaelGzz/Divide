package com.ragl.divide.ui.screens

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel: ViewModel() {
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
    fun isFieldsValid(): Boolean {
        return validateEmail() && validatePassword()
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