package com.ragl.divide.ui.screens.signIn

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LogInViewModel: ViewModel() {
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
        if(!password.contains(' ')) this.password = password
    }
    fun isFieldsValid(): Boolean {
        return validateEmail().and(validatePassword())
    }
    private fun validateEmail(): Boolean {
        if(email.isBlank()){
            emailError = "Email is required"
            return false
        }
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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