package com.ragl.divide.ui.screens.group

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
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

    private var _group = MutableStateFlow(Group())
    var group = _group.asStateFlow()

    var members by mutableStateOf<List<User>>(emptyList())
        private set

    var selectedImageUri by mutableStateOf<Uri>(Uri.EMPTY)
        private set
    var nameError by mutableStateOf("")
        private set

    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    fun updateName(name: String) {
        _group.update {
            it.copy(name = name)
        }
    }

    fun updateImage(image: Uri) {
        selectedImageUri = image
    }

    fun addUser(userId: String) {
        _group.update {
            it.copy(users = it.users.apply { set(userId, userId) })
        }
    }

    fun removeUser(userId: String) {
        _group.update {
            it.copy(users = it.users.apply { remove(userId) })
        }
    }

    fun setGroup(group: Group) {
        viewModelScope.launch {
            _isLoading.update { true }
            _group.update {
                group
            }
            members = groupRepository.getUsers(_group.value.users.values.toList())
            _isLoading.update { false }
        }
    }

    fun leaveGroup(onSuccessful: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                groupRepository.leaveGroup(_group.value.id)
                onSuccessful()
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", e.message, e)
                onError(e.message ?: "An error occurred")
            }
        }
    }

    private fun validateName(): Boolean {
        return when (_group.value.name) {
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

    fun saveGroup(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (validateName()) {
            _group.update {
                it.copy(name = it.name.trim())
            }
            viewModelScope.launch {
                try {
                    _isLoading.update { true }
                    groupRepository.saveGroup(
                        _group.value,
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

    fun deleteGroup(onDelete: () -> Unit) {
        viewModelScope.launch {
            try {
                groupRepository.deleteGroup(
                    _group.value.id,
                    if (_group.value.image.isNotEmpty()) _group.value.id else ""
                )
                onDelete()
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", e.message, e)
            }
        }
    }
}