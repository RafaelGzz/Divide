package com.ragl.divide.ui.screens.expenseDetails

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.Expense
import com.ragl.divide.ui.screens.home.TitleRow
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.theme.AppTypography
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

@Composable
fun ExpenseDetailsScreen(
    expenseDetailsViewModel: ExpenseDetailsViewModel = hiltViewModel(),
    expenseId: String,
    editExpense: (String) -> Unit,
    onBackClick: () -> Unit,
    onDeleteExpense: () -> Unit
) {

    LaunchedEffect(Unit) {
        expenseDetailsViewModel.setExpense(expenseId)
    }

    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var onConfirmDeleteClick by remember { mutableStateOf({}) }
    var dialogMessage by remember { mutableIntStateOf(0) }

    var isPaymentDialogVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val expense by expenseDetailsViewModel.expense.collectAsState()
    val isLoading by expenseDetailsViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            if (!isLoading)
                ExpenseDetailsAppBar(
                    expense = expense,
                    onBackClick = onBackClick,
                    onEditClick = {
                        editExpense(expenseId)
                    },
                    onDeleteButtonClick = {
                        dialogMessage = R.string.delete_expense_confirm
                        onConfirmDeleteClick = {
                            expenseDetailsViewModel.deleteExpense(
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
                        amount = expense.amount,
                        amountPaid = expense.amountPaid,
                        numberOfPayments = expense.numberOfPayments,
                        onDismissRequest = { isPaymentDialogVisible = false },
                        onConfirmClick = { amount ->
                            expenseDetailsViewModel.addPayment(amount) { showToast(context, it) }
                            isPaymentDialogVisible = false
                        }
                    )
                }
                Text(
                    text = NumberFormat.getCurrencyInstance().format(expense.amount),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.s_paid,
                        NumberFormat.getCurrencyInstance().format(expense.amountPaid)
                    ),
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
                            headlineContent = { Text(NumberFormat.getCurrencyInstance().format(it.value.amount)) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    amountPaid: Double,
    amount: Double,
    numberOfPayments: Int,
) {
    val onePayment = amount / numberOfPayments
    val remainingBalance = amount - amountPaid

    var paymentAmount by remember { mutableStateOf("") }
    var paymentAmountError by remember { mutableStateOf("") }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
//    var custom by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.remaining_balance, NumberFormat.getCurrencyInstance().format(amount - amountPaid)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.select_payment_or_custom),
                    style = AppTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                ) {
                    TextField(
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        value = paymentAmount,
                        onValueChange = { if (it.isDigitsOnly()) paymentAmount = it },
                        singleLine = true,
                        prefix = {
                                 Text(text = "$")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Go,
                            autoCorrectEnabled = false
                        ),
                        readOnly = false,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .clip(ShapeDefaults.Medium)
                    )
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .heightIn(max = 230.dp)
                    ) {
                        for (i in 1..numberOfPayments) {
                            val q = i * onePayment
                            if(q <= remainingBalance)
                                DropdownMenuItem(
                                    text = { Text(
                                        stringResource(
                                            R.string.payment,
                                            NumberFormat.getCurrencyInstance().format(q),
                                            i,
                                            if (i > 1) "s" else ""
                                        )) },
                                    onClick = {
                                        paymentAmount = (q).toString()
                                        categoryMenuExpanded = false
                                    },
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                                )
                        }
                    }
                }
                if (paymentAmountError != "")
                    Text(
                        text = paymentAmountError,
                        color = MaterialTheme.colorScheme.error,
                        style = AppTypography.labelSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (paymentAmount.isNotEmpty() && paymentAmount.toDouble().toLong() > 0) {
                    if (paymentAmount.toDouble().toLong() <= remainingBalance)
                        onConfirmClick(paymentAmount.toDouble().toLong())
                    else paymentAmountError =
                        context.getString(R.string.amount_must_be_less_than_remaining_balance)
                } else paymentAmountError = context.getString(R.string.required)
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