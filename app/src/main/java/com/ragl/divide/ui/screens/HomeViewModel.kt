package com.ragl.divide.ui.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private var auth: FirebaseAuth
) : ViewModel() {

    fun logout(onSignOut: () -> Unit) {
        auth.signOut()
        auth.addAuthStateListener {
            if (it.currentUser == null)
                onSignOut()
        }
    }

}