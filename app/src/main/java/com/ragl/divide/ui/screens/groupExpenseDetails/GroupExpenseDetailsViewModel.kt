package com.ragl.divide.ui.screens.groupExpenseDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.repositories.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupExpenseDetailsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _groupExpense = MutableStateFlow(GroupExpense())
    val groupExpense = _groupExpense.asStateFlow()

    fun setGroupExpense(groupExpense: GroupExpense) {
        _groupExpense.update {
            groupExpense
        }
    }

    fun deleteExpense(groupId: String, onSuccess: (GroupExpense) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                groupRepository.deleteExpense(groupId, groupExpense.value)
                onSuccess(groupExpense.value)
            } catch (e: Exception) {
                onError(e.message ?: "An error occurred")
            }
        }
    }

}