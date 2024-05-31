package com.ragl.divide.ui.screens.expenseDetails

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _expense = MutableStateFlow(Expense())
    val expense = _expense.asStateFlow()

    val remainingBalance = mutableDoubleStateOf(0.0)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    fun setExpense(expenseId: String) {
        viewModelScope.launch {
            _isLoading.update {
                true
            }
            _expense.update {
                userRepository.getExpense(expenseId)
            }
            remainingBalance.value = _expense.value.amount - _expense.value.amountPaid
            _isLoading.update {
                false
            }
        }
    }

    fun deleteExpense(id: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteExpense(id)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }

    fun deletePayment(paymentId: String, amount: Double, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteExpensePayment(paymentId, _expense.value.id)
                _expense.update {
                    it.copy(
                        payments = userRepository.getExpensePayments(_expense.value.id),
                        amountPaid = it.amountPaid - amount
                    )
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }

    fun addPayment(amount: Long, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.addExpensePayment(
                    Payment(amount = amount.toDouble()),
                    expenseId = _expense.value.id
                )
                _expense.update {
                    it.copy(
                        payments = userRepository.getExpensePayments(_expense.value.id),
                        amountPaid = it.amountPaid + amount
                    )
                }
                remainingBalance.value = _expense.value.amount - _expense.value.amountPaid
            } catch (e: Exception) {
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }
}