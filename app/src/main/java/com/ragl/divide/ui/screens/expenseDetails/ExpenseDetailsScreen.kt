package com.ragl.divide.ui.screens.expenseDetails

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.ui.screens.home.TitleRow
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.toTwoDecimals
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

@Composable
fun ExpenseDetailsScreen(
    expenseDetailsViewModel: ExpenseDetailsViewModel = hiltViewModel(),
    expense: Expense,
    editExpense: (String) -> Unit,
    onPaymentMade: (Payment) -> Unit,
    onDeletePayment: (String) -> Unit,
    onBackClick: () -> Unit,
    onDeleteExpense: () -> Unit,
    onPaidExpense: () -> Unit,
) {

    BackHandler {
        onBackClick()
    }

    LaunchedEffect(Unit) {
        expenseDetailsViewModel.setExpense(expense)
    }

    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var onConfirmDeleteClick by remember { mutableStateOf({}) }
    var dialogMessage by remember { mutableIntStateOf(0) }

    var isPaymentDialogVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val expenseState by expenseDetailsViewModel.expense.collectAsState()
    val isLoading by expenseDetailsViewModel.isLoading.collectAsState()

    val remainingBalance = remember(expenseState.amountPaid, expenseState.amount) {
        (expenseState.amount - expenseState.amountPaid).toTwoDecimals()
    }

    Scaffold(
        topBar = {
            if (!isLoading)
                ExpenseDetailsAppBar(
                    expense = expenseState,
                    onBackClick = onBackClick,
                    onEditClick = {
                        editExpense(expense.id)
                    },
                    onDeleteButtonClick = {
                        dialogMessage = R.string.delete_expense_confirm
                        onConfirmDeleteClick = {
                            expenseDetailsViewModel.deleteExpense(
                                expenseState.id,
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
        if (isLoading)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        AnimatedVisibility(
            visible = !isLoading, enter = fadeIn(animationSpec = tween(200)),
            exit = ExitTransition.None
        ) {
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
                            expenseDetailsViewModel.addPayment(
                                amount = amount,
                                onSuccess = onPaymentMade,
                                onFailure = { showToast(context, it) },
                                onPaidExpense = onPaidExpense
                            )
                            isPaymentDialogVisible = false
                        }
                    )
                }
                Text(
                    text = NumberFormat.getCurrencyInstance().format(expenseState.amount),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
//                Text(
//                    text = stringResource(
//                        R.string.s_paid,
//                        NumberFormat.getCurrencyInstance().format(expense.amountPaid)
//                    ),
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.labelMedium,
//                    modifier = Modifier.fillMaxWidth()
//                )
                Text(
                    text = stringResource(
                        R.string.remaining_balance,
                        NumberFormat.getCurrencyInstance().format(remainingBalance)
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(
                        R.string.added_on,
                        DateFormat.getDateInstance(DateFormat.LONG)
                            .format(Date(expenseState.addedDate))
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                if (expenseState.notes != "") {
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
                        text = expenseState.notes,
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
                    if (expenseState.payments.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_movements_yet),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp)
                            )
                        }
                    }
                    items(
                        expenseState.payments.entries.toList().sortedBy { it.value.date },
                        key = { it.key }) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            leadingContent = {
                                Text(
                                    stringResource(R.string.paid),
                                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                )
                            },
                            headlineContent = {
                                Text(
                                    NumberFormat.getCurrencyInstance().format(it.value.amount)
                                )
                            },
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
                                            expenseDetailsViewModel.deletePayment(
                                                paymentId = it.key,
                                                amount = it.value.amount,
                                                onFailure = { showToast(context, it) },
                                                onSuccess = { onDeletePayment(it.key) }
                                            )
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
                    if (expenseState.payments.isNotEmpty())
                        item {
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                leadingContent = {
                                    Text(
                                        stringResource(R.string.total),
                                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        NumberFormat.getCurrencyInstance()
                                            .format(expenseState.amountPaid),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clip(ShapeDefaults.Medium)
                            )
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDetailsAppBar(
    expense: Expense,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteButtonClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                expense.title,
                softWrap = true,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

        },
        actions = {
            IconButton(onClick = onEditClick) {
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
            Text(stringResource(R.string.delete), style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(stringResource(res), style = MaterialTheme.typography.bodySmall)
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@Composable
fun PaymentAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Double) -> Unit,
    remainingBalance: Double
) {

    var paymentAmount by remember { mutableStateOf("") }
    var paymentAmountError: String? by remember { mutableStateOf(null) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .99f),
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                stringResource(R.string.make_a_payment),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                DivideTextField(
                    label = stringResource(R.string.amount),
                    input = paymentAmount,
                    error = paymentAmountError,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Go,
//                    errorText = paymentAmountError != "",
                    prefix = { Text(text = "$") },
                    onValueChange = { input ->
                        if (input.isEmpty()) paymentAmount = "" else {
                            val formatted = input.replace(",", ".")
                            val parsed = formatted.toDoubleOrNull()
                            parsed?.let {
                                val decimalPart = formatted.substringAfter(".", "")
                                if (decimalPart.length <= 2 && parsed <= 999999999.99) {
                                    paymentAmount = input
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(
                        R.string.remaining_balance,
                        NumberFormat.getCurrencyInstance().format(remainingBalance)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (paymentAmount.isNotEmpty() && paymentAmount.toDouble() > 0) {
                        if (paymentAmount.toDouble() <= remainingBalance)
                            onConfirmClick(paymentAmount.toDouble())
                        else paymentAmountError =
                            context.getString(R.string.amount_must_be_less_than_remaining_balance)
                    } else paymentAmountError =
                        context.getString(R.string.amount_must_be_greater_than_0)
                }
            ) {
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