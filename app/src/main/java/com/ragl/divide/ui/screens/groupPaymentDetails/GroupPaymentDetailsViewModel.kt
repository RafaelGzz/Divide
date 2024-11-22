package com.ragl.divide.ui.screens.groupPaymentDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.repositories.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GroupPaymentDetailsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {


    private val _group = MutableStateFlow(Group())
    val group = _group.asStateFlow()

    private val _payment = MutableStateFlow(Payment())
    val payment = _payment.asStateFlow()

    private var _groupUser = MutableStateFlow(GroupUser())
    var groupUser = _groupUser.asStateFlow()

    var amount by mutableStateOf("")
        private set
    var amountError by mutableStateOf("")
        private set
    var isUpdate by mutableStateOf(false)
        private set

    var owedUsers by mutableStateOf(listOf<User>())
        private set
    var selectedOwedUser by mutableStateOf(User())
        private set

    fun setGroupAndPayment(
        group: Group,
        members: List<User>,
        payment: Payment,
        groupUser: GroupUser
    ) {
        if (payment.id.isNotEmpty()) isUpdate = true
        _groupUser.update { groupUser }
        _group.update { group }
        _payment.update { payment }
        owedUsers = members.filter { it.uuid in _groupUser.value.debts.keys }.sortedBy { it.name }
        selectedOwedUser = owedUsers.first()
    }

    fun updateSelectedOwedUser(user: User) {
        selectedOwedUser = user
    }

    fun updateAmount(amount: String) {
        this.amount = amount
    }

    private fun validateAmount(): Boolean {
        amountError = if (amount.isEmpty()) "Ingresa una cantidad" else ""
        return amountError.isEmpty()
    }

    fun makePayment() {
        if(validateAmount()){
            //do something
        }
    }
}
