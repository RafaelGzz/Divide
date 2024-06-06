package com.ragl.divide.ui.screens.expense

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private var id = ""
    var title by mutableStateOf("")
        private set
    var titleError by mutableStateOf("")
        private set
    var amount by mutableStateOf("")
        private set
    var amountError by mutableStateOf("")
        private set
    var category by mutableStateOf(Category.GENERAL)
        private set
    var payments by mutableStateOf("1")
        private set
    var paymentsError by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set
    var reminders by mutableStateOf(false)
        private set
    var frequency by mutableStateOf(Frequency.DAILY)
        private set
    var startingDate by mutableLongStateOf(Date().time)
        private set

    fun updateTitle(title: String) {
        this.title = title
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

    fun updateAmount(amount: String) {
        this.amount = amount
    }

    private fun validateAmount(): Boolean {
        return when (amount.trim()) {
            "" -> {
                this.amountError = "Amount is required"
                false
            }

            else -> {
                this.amountError = ""
                true
            }
        }
    }

    fun updateCategory(category: Category) {
        this.category = category
    }

    fun updateNotes(notes: String) {
        this.notes = notes
    }

    fun updatePayments(payments: String) {
        if (payments.isNotEmpty() && payments.isDigitsOnly() && payments.toInt() <= 0) this.payments =
            "1"
        else this.payments = payments
    }

    private fun validatePayments(): Boolean {
        return when (payments.trim()) {
            "" -> {
                this.paymentsError = "Payments is required"
                false
            }

            else -> {
                this.paymentsError = ""
                true
            }
        }
    }

    fun updateReminders(reminders: Boolean) {
        this.reminders = reminders
    }

    fun updateFrequency(frequency: Frequency) {
        this.frequency = frequency
    }

    fun updateStartingDate(startingDate: Long) {
        this.startingDate = startingDate
    }

    fun saveExpense(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (validateTitle() && validateAmount() && validatePayments()) {
            viewModelScope.launch {
                try {
                    userRepository.saveExpense(
                        Expense(
                            id = id,
                            title = title,
                            amount = amount.toDouble(),
                            category = category,
                            reminders = reminders,
                            numberOfPayments = payments.toInt(),
                            payments = if (id.isNotEmpty()) userRepository.getExpensePayments(id) else emptyMap(),
                            notes = notes,
                            frequency = frequency,
                            startingDate = startingDate
                        )
                    )
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun setViewModelExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                val expense = userRepository.getExpense(expenseId)
                id = expense.id
                title = expense.title
                amount = expense.amount.toString()
                category = expense.category
                reminders = expense.reminders
                payments = expense.numberOfPayments.toString()
                notes = expense.notes
                frequency = expense.frequency
                startingDate = expense.startingDate
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Error fetching expense: ${e.message}")
            }
        }
    }
}