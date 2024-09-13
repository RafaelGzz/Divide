package com.ragl.divide.ui.screens.groupExpense

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
    var userId by mutableStateOf("")
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
    var selectedMembers by mutableStateOf<List<String>>(listOf())
        private set
    var quantities by mutableStateOf<Map<String, Double>>(emptyMap())
        private set
    var percentages by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var amountPerPerson by mutableDoubleStateOf(0.0)
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
    fun updateQuantities(quantities: Map<String, Double>) {
        this.quantities = quantities
    }

    fun updatePercentages(percentages: Map<String, Int>) {
        this.percentages = percentages
    }

    fun updateAmountPerPerson(amountPerPerson: Double) {
        this.amountPerPerson = amountPerPerson
    }

    fun updateSelectedMembers(selectedMembers: List<String>) {
        this.selectedMembers = selectedMembers
    }

    fun setGroupAndExpense(group: Group, userId: String, members: List<User>, expense: GroupExpense) {
        viewModelScope.launch {
            if(expense.id.isNotEmpty()){
                _expense.update { expense }
                title = expense.title
                amount = expense.amount.let { if(it == 0.0) "" else it.toString() }
                paidBy = members.firstOrNull { it.uuid == expense.paidBy.keys.first() }!!
                method = expense.splitMethod

                when (expense.splitMethod) {
                    Method.EQUALLY -> {
                        selectedMembers = expense.debtors.keys.toList()
                        amountPerPerson = expense.debtors.values.first()
                        percentages = members.associate { it.uuid to 0 }
                        quantities = members.associate { it.uuid to 0.0 }
                    }
                    Method.PERCENTAGES -> {
                        selectedMembers = expense.debtors.keys.toList()
                        quantities = members.associate { it.uuid to 0.0 }
                        percentages = expense.debtors.mapValues { it.value.toInt() }
                    }
                    Method.CUSTOM -> {
                        selectedMembers = expense.debtors.keys.toList()
                        percentages = members.associate { it.uuid to 0 }
                        quantities = expense.debtors
                    }
                }
            } else {
                selectedMembers = members.map { it.uuid }
                percentages = members.associate { it.uuid to 0 }
                quantities = members.associate { it.uuid to 0.0 }
            }

            _group.update { group }
            updateMembers(members)
            paidBy = members.first { it.uuid == userId }
        }
        this.userId = userId
    }

    private fun updateMembers(members: List<User>) {
        this.members = members
    }

    fun validateTitle(): Boolean {
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

    fun validateAmount(): Boolean {
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

    fun saveEquallyExpense(
        onSuccess: (GroupExpense) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (validateTitle() && validateAmount()) {
                val expense = _expense.value.copy(
                    title = title,
                    amount = amount.toDouble(),
                    paidBy = if(paidBy.uuid in selectedMembers) mapOf(paidBy.uuid to amount.toDouble() - amountPerPerson) else mapOf(paidBy.uuid to amount.toDouble()),  //mapOf(paidBy.uuid to amount.toDouble() - amountPerPerson),
                    splitMethod = method,
                    debtors = selectedMembers.associateWith { amountPerPerson }.filter { it.key != paidBy.uuid }
                )
                viewModelScope.launch {
                    val savedExpense = groupRepository.saveExpense(
                        groupId = _group.value.id,
                        expense = expense,
                        currentUserId = userId
                    )
                    onSuccess(savedExpense)
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }

    fun savePercentageExpense(
        onSuccess: (GroupExpense) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (validateTitle() && validateAmount()) {
                val expense = _expense.value.copy(
                    title = title,
                    amount = amount.toDouble(),
                    paidBy = mapOf(paidBy.uuid to amount.toDouble()),
                    splitMethod = method,
                    debtors = percentages.mapValues {
                        it.value.toDouble()
                    }.filter { it.key != paidBy.uuid }
                )
                viewModelScope.launch {
                    val savedExpense = groupRepository.saveExpense(
                        groupId = _group.value.id,
                        expense = expense,
                        currentUserId = userId
                    )
                    onSuccess(savedExpense)
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }

    fun saveCustomExpense(
        onSuccess: (GroupExpense) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            if (validateTitle() && validateAmount()) {
                val expense = _expense.value.copy(
                    title = title,
                    amount = amount.toDouble(),
                    paidBy = mapOf(paidBy.uuid to amount.toDouble()),
                    splitMethod = method,
                    debtors = quantities.filter { it.key != paidBy.uuid }
                )
                viewModelScope.launch {
                    val savedExpense = groupRepository.saveExpense(
                        groupId = _group.value.id,
                        expense = expense,
                        currentUserId = userId
                    )
                    onSuccess(savedExpense)
                }
            }
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }
}