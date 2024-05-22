package com.ragl.divide.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.ragl.divide.BuildConfig
import com.ragl.divide.data.PreferencesRepository
import com.ragl.divide.data.UserRepository
import com.ragl.divide.data.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    var user by mutableStateOf<User?>(null)
        private set

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    private var _startDestination = MutableStateFlow("Splash")
    var startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            user = userRepository.getDatabaseUser()
            preferencesRepository.startDestinationFlow.collect {
                _startDestination.value = it.ifEmpty { "Login" }
            }
            _isLoading.value = false
            Log.i("UserViewModel", "init: $isLoading")
        }
    }

    fun signInWithEmailAndPassword(
        email: String, password: String,
        onSuccessfulLogin: () -> Unit,
        onFailedLogin: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                user = userRepository.signInWithEmailAndPassword(email, password)
                if (user != null) {
                    preferencesRepository.saveStartDestination("Home")
                    onSuccessfulLogin()
                } else onFailedLogin("Failed to Log in")
            } catch (e: Exception) {
                Log.e("UserViewModel", "signInWithEmailAndPassword: ", e)
                onFailedLogin(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String, password: String, name: String,
        onSuccessfulLogin: () -> Unit,
        onFailedLogin: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                user = userRepository.signUpWithEmailAndPassword(email, password, name)
                if (user != null) {
                    preferencesRepository.saveStartDestination("Home")
                    onSuccessfulLogin()
                } else onFailedLogin("Failed to Log in")
            } catch (e: Exception) {
                onFailedLogin(e.message ?: "Unknown error")
                Log.e("UserViewModel", "signUpWithEmailAndPassword: ", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        onSuccessfulLogin: () -> Unit,
        onFailedLogin: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val authCredential = getAuthCredential(context)
                user = userRepository.signInWithCredential(
                    authCredential
                )
                if (user != null) {
                    preferencesRepository.saveStartDestination("Home")
                    onSuccessfulLogin()
                } else {
                    onFailedLogin("Failed to sign in")
                }
            } catch (e: Exception) {
                onFailedLogin(e.message ?: "Unknown error")
                Log.e("UserViewModel", "signInWithGoogle: ", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getAuthCredential(context: Context): AuthCredential {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption =
            GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credential =
            credentialManager.getCredential(request = request, context = context).credential
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        return authCredential
    }
}