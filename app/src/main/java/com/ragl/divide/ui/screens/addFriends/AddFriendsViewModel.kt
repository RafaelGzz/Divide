package com.ragl.divide.ui.screens.addFriends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFriendsViewModel @Inject constructor(
    private val repository: FriendsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var searchText by mutableStateOf("")
        private set

    var users by mutableStateOf(emptyMap<String, User>())
        private set

    var selectedUser by mutableStateOf<User?>(null)
        private set

    fun updateSelectedUser(user: User) {
        selectedUser = user
    }

    fun updateSearchText(text: String) {
        searchText = text
    }

    fun searchUser(friends: List<User>) {
        if (searchText.isEmpty()) {
            users = emptyMap()
            return
        }
        viewModelScope.launch {
            isLoading = true
            users = repository.searchUsers(searchText, friends)
            isLoading = false
        }
    }

    fun addFriend(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.addFriend(selectedUser!!)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}