package com.ragl.divide.ui.screens

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private var auth: FirebaseAuth
) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var emailError by mutableStateOf("")
        private set
    var username by mutableStateOf("")
        private set
    var usernameError by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var passwordError by mutableStateOf("")
        private set
    var passwordConfirm by mutableStateOf("")
        private set
    var passwordConfirmError by mutableStateOf("")
        private set

    fun updateEmail(email: String) {
        this.email = email.trim()
    }

    fun updateUsername(username: String) {
        this.username = username.trim()
    }

    fun updatePassword(password: String) {
        this.password = password.trim()
    }

    fun updatePasswordConfirm(passwordConfirm: String) {
        this.passwordConfirm = passwordConfirm.trim()
    }

    fun tryLogin(onSuccessfulLogin: () -> Unit, onFailedLogin: () -> Unit) {
        validateEmail()
        validateUsername()
        validatePassword()
        validatePasswordConfirm()

        if (emailError.isBlank() && passwordError.isBlank() && passwordConfirmError.isBlank() && usernameError.isBlank()) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) onSuccessfulLogin()
                else onFailedLogin()
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

    private fun validateUsername(): Boolean {
        if (username.isNotBlank() && username.length >= 3) {
            usernameError = ""
            return true
        } else {
            usernameError = "Username must be at least 3 characters"
            return false
        }
    }

    private fun validatePassword(): Boolean {
        if (password.isNotBlank()) {
            if (password.length >= 8) {
                passwordError = ""
                return true
            } else {
                passwordError = "Password must be at least 8 characters"
                return false
            }
        } else {
            passwordError = "Password is required"
            return false
        }
    }

    private fun validatePasswordConfirm(): Boolean {
        if (passwordConfirm.isNotBlank() && passwordConfirm == password) {
            passwordConfirmError = ""
            return true
        } else {
            passwordConfirmError = "Passwords do not match"
            return false
        }
    }
}