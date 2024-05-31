package com.ragl.divide.ui.screens.expenseDetails

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.ragl.divide.R
import com.ragl.divide.data.models.Expense
import com.ragl.divide.ui.screens.home.TitleRow
import com.ragl.divide.ui.screens.login.DivideTextField
import com.ragl.divide.ui.showToast
import kotlinx.coroutines.flow.StateFlow
import java.text.DateFormat
import java.util.Date

@Composable
fun ExpenseDetailsScreen(
    loadExpense: () -> Unit,
    expenseState: StateFlow<Expense>,
    remainingBalanceState: MutableDoubleState,
    isLoadingState: StateFlow<Boolean>,
    deleteExpense: (String, () -> Unit, (String) -> Unit) -> Unit,
    deletePayment: (String, Double, (String) -> Unit) -> Unit,
    addPayment: (Long, (String) -> Unit) -> Unit,
    onBackClick: () -> Unit,
    onDeleteExpense: () -> Unit
) {

    LaunchedEffect(Unit) {
        loadExpense()
    }

    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var onConfirmDeleteClick by remember { mutableStateOf({}) }
    var dialogMessage by remember { mutableIntStateOf(0) }

    var isPaymentDialogVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val expense by expenseState.collectAsState()
    val isLoading by isLoadingState.collectAsState()
    val remainingBalance = remainingBalanceState.value

    Scaffold(
        topBar = {
            if (!isLoading)
                ExpenseDetailsAppBar(
                    expense = expense,
                    onBackClick = onBackClick,
                    onDeleteButtonClick = {
                        dialogMessage = R.string.delete_expense_confirm
                        onConfirmDeleteClick = {
                            deleteExpense(
                                expense.id,
                                {
                                    isDeleteDialogVisible = false
                                    onDeleteExpense()
                                },
                                { message -> showToast(context, message) }
                            )
                        }
                        isDeleteDialogVisible = true
                    }
                )
        }
    ) { paddingValues ->
        if (!isLoading)
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                if (isDeleteDialogVisible) {
                    DeleteAlertDialog(
                        onDismissRequest = { isDeleteDialogVisible = false },
                        res = dialogMessage,
                        onConfirmClick = onConfirmDeleteClick
                    )
                }
                if (isPaymentDialogVisible) {
                    PaymentAlertDialog(
                        remainingBalance = remainingBalance,
                        onDismissRequest = { isPaymentDialogVisible = false },
                        onConfirmClick = { amount ->
                            addPayment(amount) { showToast(context, it) }
                            isPaymentDialogVisible = false
                        }
                    )
                }
                Text(
                    text = "$${expense.amount}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.s_paid, expense.amountPaid),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )
//                if (expense.numberOfPayments > 1)
//                    Text(
//                        text = stringResource(R.string.s_payments, expense.numberOfPayments),
//                        textAlign = TextAlign.Center,
//                        style = MaterialTheme.typography.labelLarge,
//                        modifier = Modifier.fillMaxWidth()
//                    )
                Text(
                    text = stringResource(
                        R.string.added_on,
                        DateFormat.getDateInstance(DateFormat.LONG).format(Date(expense.addedDate))
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (expense.notes != "") {
                    Text(
                        text = "${stringResource(R.string.notes)}:",
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                    Text(
                        text = expense.notes,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TitleRow(
                    labelStringResource = R.string.movements,
                    buttonStringResource = R.string.make_a_payment,
                    onAddClick = { isPaymentDialogVisible = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 10.dp)
                )
                LazyColumn {
                    if (expense.payments.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_movements_yet),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            )
                        }
                    }
                    items(
                        expense.payments.entries.toList().sortedBy { it.value.date },
                        key = { it.key }) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            leadingContent = {
                                Text(
                                    stringResource(R.string.paid),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            headlineContent = { Text("$${it.value.amount}") },
                            supportingContent = {
                                Text(
                                    DateFormat.getDateInstance(DateFormat.LONG)
                                        .format(Date(it.value.date)),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        onConfirmDeleteClick = {
                                            deletePayment(
                                                it.key,
                                                it.value.amount
                                            ) { showToast(context, it) }
                                            isDeleteDialogVisible = false
                                        }
                                        dialogMessage = R.string.delete_payment_confirm
                                        isDeleteDialogVisible = true
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete payment")
                                }
                            },
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(ShapeDefaults.Medium)
                        )
                    }
                }
            }
        else
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDetailsAppBar(
    expense: Expense,
    onBackClick: () -> Unit,
    onDeleteButtonClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                expense.title,
                softWrap = true,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp)
            )
        },
        navigationIcon = {

            IconButton(
                onClick = onBackClick
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }

        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDeleteButtonClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    )
}

@Composable
fun DeleteAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    @StringRes res: Int
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.no))
            }
        },
        title = {
            Text(stringResource(R.string.delete))
        },
        text = {
            Text(stringResource(res))
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Composable
fun PaymentAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    remainingBalance: Double
) {
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .99f),
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(stringResource(R.string.make_a_payment))
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.remaining_balance, remainingBalance),
                    style = MaterialTheme.typography.labelSmall
                )
                DivideTextField(
                    label = stringResource(R.string.amount),
                    input = amount,
                    prefix = {
                        Text(text = "$")
                    },
                    placeholder = {
                        Text(text = "0")
                    },
                    imeAction = ImeAction.Go,
                    keyboardType = KeyboardType.NumberPassword,
                    error = amountError,
                    onValueChange = {
                        if (it.isDigitsOnly()) {
                            amount = it
                        }
                    })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (amount.isNotEmpty() && amount.toLong() > 0) {
                    if (amount.toLong() <= remainingBalance)
                        onConfirmClick(amount.toLong())
                    else amountError = "Amount must be less than remaining balance"
                } else amountError = "Required"
            }) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}