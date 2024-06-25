package com.ragl.divide.ui.screens.signIn

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.ragl.divide.BuildConfig
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    private var _startDestination = MutableStateFlow<Screen>(Screen.Splash)
    var startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.startDestinationFlow.collect {
                _startDestination.value = Screen.stringToScreen(it)
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
                if (userRepository.signInWithEmailAndPassword(email, password) != null) {
                    preferencesRepository.saveStartDestination(Screen.Home.route)
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
                if (userRepository.signUpWithEmailAndPassword(email, password, name) != null) {
                    preferencesRepository.saveStartDestination(Screen.Home.route)
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
                if (userRepository.signInWithCredential(authCredential) != null) {
                    preferencesRepository.saveStartDestination(Screen.Home.route)
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
//            GetGoogleIdOption.Builder()
//                .setFilterByAuthorizedAccounts(false)
//                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
//                .setAutoSelectEnabled(false)
//                .build()
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