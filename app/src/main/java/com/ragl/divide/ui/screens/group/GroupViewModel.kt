package com.ragl.divide.ui.screens.group

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.repositories.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private var _state = MutableStateFlow(Group())
    var state = _state.asStateFlow()

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set
    var nameError by mutableStateOf("")
        private set
    var imageError by mutableStateOf("")
        private set
    var usersError by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun updateName(name: String) {
        _state.update {
            it.copy(name = name)
        }
    }

    fun updateImage(image: Uri?) {
        selectedImageUri = image
    }

    fun addUser(user: String) {
        _state.update {
            it.copy(users = it.users + user)
        }
    }

    fun removeUser(user: String) {
        _state.update {
            it.copy(users = it.users - user)
        }
    }

    fun setGroup(id: String) {
        viewModelScope.launch {
            isLoading = true
            _state.update {
                groupRepository.getGroup(id)
            }
            isLoading = false
        }
    }

    private fun validateName(): Boolean {
        return when (_state.value.name.trim()) {
            "" -> {
                this.nameError = "Title is required"
                false
            }

            else -> {
                this.nameError = ""
                true
            }
        }
    }

    private fun validateImage(): Boolean {
        return when (_state.value.image.trim()) {
            "" -> {
                this.imageError = "Image is required"
                false
            }

            else -> {
                this.imageError = ""
                true
            }
        }
    }

    fun saveGroup(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (validateName()) {
            viewModelScope.launch {
                try {
                    groupRepository.saveGroup(
                        _state.value,
                        selectedImageUri
                    )
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

}