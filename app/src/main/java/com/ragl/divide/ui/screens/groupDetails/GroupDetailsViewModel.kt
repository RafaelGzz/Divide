package com.ragl.divide.ui.screens.groupDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GroupDetailsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _group = MutableStateFlow(Group())
    val group = _group.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    var expensesAndPayments by mutableStateOf<List<Any>>(listOf())
        private set

    private fun updateExpensesAndPayments(expensesAndPayments: List<Any>) {
        this.expensesAndPayments = expensesAndPayments
    }

    var members by mutableStateOf<List<User>>(listOf())
        private set

    var groupUser by mutableStateOf(GroupUser())
        private set

    fun setGroup(group: Group, userId: String, members: List<User>) {

        _group.update {
            group
        }
        this.groupUser = group.users[userId]!!
        updateMembers(members)
        updateExpensesAndPayments(group.expenses.values.toList() + group.payments.values.toList())

    }

    fun getPaidByNames(paidBy: List<String>): String {
        return paidBy.map { uid ->
            members.find { it.uuid == uid }?.name
        }.joinToString(", ") // Une los nombres con comas
    }

    private fun updateMembers(members: List<User>) {
        this.members = members
    }

}