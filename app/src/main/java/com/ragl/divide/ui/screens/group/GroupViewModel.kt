package com.ragl.divide.ui.screens.group

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    var selectedImageUri by mutableStateOf<Uri>(Uri.EMPTY)
        private set
    var nameError by mutableStateOf("")
        private set
    var imageError by mutableStateOf("")
        private set
    var usersError by mutableStateOf("")
        private set

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    fun updateName(name: String) {
        _state.update {
            it.copy(name = name)
        }
    }

    fun updateImage(image: Uri) {
        selectedImageUri = image
    }

    fun addUser(userId: String) {
        _state.update {
            it.copy(users = it.users.apply { set(userId, userId) })
        }
    }

    fun removeUser(userId: String) {
        _state.update {
            it.copy(users = it.users.apply { remove(userId) })
        }
    }

    fun setGroup(id: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            _state.update {
                groupRepository.getGroup(id)
            }
            _isLoading.update { false }
        }
    }

    private fun validateName(): Boolean {
        return when (_state.value.name) {
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
            _state.update {
                it.copy(name = it.name.trim())
            }
            viewModelScope.launch {
                try {
                    _isLoading.update { true }
                    groupRepository.saveGroup(
                        _state.value,
                        selectedImageUri
                    )
                    _isLoading.update { false }
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("GroupViewModel", e.message, e)
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

}