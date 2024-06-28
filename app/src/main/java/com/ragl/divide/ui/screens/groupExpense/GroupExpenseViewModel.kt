package com.ragl.divide.ui.screens.groupExpense

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Method
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupExpenseViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _group = MutableStateFlow(Group())
    val group = _group.asStateFlow()

    private val _expense = MutableStateFlow(GroupExpense())
    val expense = _expense.asStateFlow()
    var title by mutableStateOf("")
        private set
    var titleError by mutableStateOf("")
        private set
    var amount by mutableStateOf("")
        private set
    var amountError by mutableStateOf("")
        private set
    var members by mutableStateOf<List<User>>(listOf())
        private set
    var paidBy by mutableStateOf(User())
        private set
    var method by mutableStateOf(Method.EQUALLY)
        private set

    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateAmount(amount: String) {
        this.amount = amount
    }

    fun updatePaidBy(user: User) {
        paidBy = user
    }

    fun updateMethod(method: Method) {
        this.method = method
    }

    fun setGroup(group: Group, userId: String) {
        viewModelScope.launch {
            _group.update {
                group
            }
            members = groupRepository.getUsers(_group.value.users.values.toList()).toMutableList()
            paidBy = members.first { it.uuid == userId }
        }
    }

    private fun validateTitle(): Boolean {
        return when (title.trim()) {
            "" -> {
                this.titleError = "Title is required"
                false
            }

            else -> {
                this.titleError = ""
                true
            }
        }
    }

    private fun validateAmount(): Boolean {
        if (amount.isEmpty()) {
            this.amountError = "Amount is required"
            return false
        }

        val amountDouble = amount.toDoubleOrNull() ?: run {
            this.amountError = "Invalid amount"
            return false
        }

        if (amountDouble <= 0) {
            this.amountError = "Amount must be greater than 0"
            return false
        }

        this.amountError = ""
        return true
    }

    fun saveExpense(onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            if (validateTitle() && validateAmount()) {
                viewModelScope.launch {
                    groupRepository.saveExpense(
                        groupId = _group.value.id,
                        _expense.value.copy(
                            title = title,
                            amount = amount.toDouble(),
                            paidBy = mapOf(paidBy.uuid to paidBy.uuid),
                            method = method
                        )
                    )
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }
}