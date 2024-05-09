package com.ragl.divide.ui.screens

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ragl.divide.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    fun signInWithGoogle(
        context: Context,
        coroutineScope: CoroutineScope,
        onSuccessfulLogin: () -> Unit,
        onFailedLogin: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption =
            GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(request = request, context = context)
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(authCredential).addOnCompleteListener { task ->
                    if (task.isSuccessful) onSuccessfulLogin()
                }.addOnFailureListener {
                    onFailedLogin(it.message.orEmpty())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}