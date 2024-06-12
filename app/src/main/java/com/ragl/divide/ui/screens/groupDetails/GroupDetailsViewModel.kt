package com.ragl.divide.ui.screens.groupDetails

import android.util.Log
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
class GroupDetailsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _group = MutableStateFlow(Group())
    val group = _group.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun setGroup(groupId: String) {
        viewModelScope.launch {
            _isLoading.update {
                true
            }
            _group.update {
                groupRepository.getGroup(groupId)
            }
            _isLoading.update {
                false
            }
        }
    }

    fun addUser(userId: String) {
        viewModelScope.launch {
            _isLoading.update {
                true
            }
            groupRepository.addUser(userId, _group.value.id)
            _isLoading.update {
                false
            }
        }
    }

    fun leaveGroup(onSuccessful: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try{
                groupRepository.leaveGroup(_group.value.id)
            }catch (e: Exception){
                Log.e("GroupDetailsViewModel", e.message, e)
                onError(e.message ?: "An error occurred")
            }
        }
    }
}