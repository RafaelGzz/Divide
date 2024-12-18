package com.ragl.divide.ui.screens.expenseDetails

import android.util.Log
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    fun setExpense(expense: Expense) {
        viewModelScope.launch {
            _expense.update {
                expense
            }
        }
    }

    fun deleteExpense(id: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteExpense(id)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ExpenseDetailsViewModel", e.message, e)
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }

    fun deletePayment(paymentId: String, amount: Double, onFailure: (String) -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteExpensePayment(paymentId, amount, _expense.value.id)
                _expense.update {
                    it.copy(
                        payments = userRepository.getExpensePayments(_expense.value.id),
                        amountPaid = it.amountPaid - amount
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("ExpenseDetailsViewModel", e.message, e)
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }

    fun addPayment(amount: Double, onSuccess: (Payment) -> Unit, onFailure: (String) -> Unit, onPaidExpense: () -> Unit) {
        viewModelScope.launch {
            try {
                val savedPayment = userRepository.saveExpensePayment(
                    Payment(amount = amount),
                    expenseId = _expense.value.id,
                    expensePaid = _expense.value.amountPaid + amount == _expense.value.amount
                )
                onSuccess(savedPayment)
                if (_expense.value.amountPaid + amount == _expense.value.amount) onPaidExpense()
                else _expense.update {
                    it.copy(
                        payments = userRepository.getExpensePayments(_expense.value.id),
                        amountPaid = it.amountPaid + amount,
                        paid = (it.amountPaid + amount) == it.amount
                    )
                }
            } catch (e: Exception) {
                Log.e("ExpenseDetailsViewModel", e.message, e)
                onFailure(e.message ?: "Something went wrong")
            }
        }
    }
}