package com.ragl.divide.ui.screens.addFriends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFriendsViewModel @Inject constructor(
    private val repository: FriendsRepository
): ViewModel(){

    var isLoading by mutableStateOf(false)
        private set

    var searchText by mutableStateOf("")
        private set

    var friends by mutableStateOf(emptyMap<String, User>())
        private set

    fun updateSearchText(text: String){
        searchText = text
    }

    fun getFriends(){
        if(searchText.isEmpty()) {
            friends = emptyMap()
            return
        }
        viewModelScope.launch{
            isLoading = true
            friends = repository.searchUsers(searchText)
            isLoading = false
        }
    }

}