package com.ragl.divide.ui.screens.groupExpenseDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Method
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.utils.FriendItem
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupExpenseDetailsScreen(
    groupExpenseDetailsViewModel: GroupExpenseDetailsViewModel = hiltViewModel(),
    groupId: String,
    groupExpense: GroupExpense,
    members: List<User>,
    onEditClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onDeleteExpense: (GroupExpense) -> Unit
) {
    LaunchedEffect(Unit) {
        groupExpenseDetailsViewModel.setGroupExpense(groupExpense)
    }

    val groupExpenseState by groupExpenseDetailsViewModel.groupExpense.collectAsState()

    val context = LocalContext.current

    val sortedDebtors = remember(groupExpense) {
        groupExpense.debtors.entries.filter { it.value != 0.0 }
            .mapNotNull { (memberId, debt) ->
                members.find { it.uuid == memberId }?.let { member -> // Use let for conciseness
                    member to debt // Create a Pair of (member, debt)
                }
            }
            .sortedBy { (member, _) -> member.name.lowercase() }.toMap()
    }
    val sortedPaidBy = remember(groupExpense) {
        groupExpense.paidBy.entries.filter { it.value != 0.0 }
            .mapNotNull { (memberId, debt) ->
                members.find { it.uuid == memberId }?.let { member -> // Use let for conciseness
                    member to debt // Create a Pair of (member, debt)
                }
            }
            .sortedBy { (member, _) -> member.name.lowercase() }.toMap()
    }

    var isDeleteDialogEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = groupExpenseState.title) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEditClick(groupExpenseState.id) }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = { isDeleteDialogEnabled = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .padding(pv)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isDeleteDialogEnabled) {
                AlertDialog(
                    onDismissRequest = { isDeleteDialogEnabled = false },
                    title = { Text(stringResource(R.string.delete), style = MaterialTheme.typography.titleMedium) },
                    text = { Text(stringResource(R.string.delete_expense_confirm), style = MaterialTheme.typography.bodyMedium) },
                    confirmButton = {
                        TextButton(onClick = {
                            isDeleteDialogEnabled = false
                            groupExpenseDetailsViewModel.deleteExpense(groupId, onDeleteExpense) {
                                showToast(context, it)
                            }
                        }) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isDeleteDialogEnabled = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            ExpenseDetails(groupExpenseState)

            Spacer(modifier = Modifier.height(16.dp))

            sortedPaidBy.forEach { (member, debt) ->
                val realDebt = when(groupExpense.splitMethod) {
                    Method.EQUALLY,Method.CUSTOM -> debt
                    Method.PERCENTAGES -> debt / 100 * groupExpense.amount
                }
                FriendItem(
                    headline = member.name,
                    photoUrl = member.photoUrl,
                    trailingContent = {
                        Text(
                            text = stringResource(
                                R.string.paid_x,
                                NumberFormat.getCurrencyInstance().format(realDebt)
                            ),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            when (groupExpense.splitMethod) {
                Method.EQUALLY -> {
                    sortedDebtors.map { (member, debt) ->
                        FriendItem(
                            headline = member.name,
                            hasLeadingContent = false,
                            trailingContent = {
                                Text(
                                    text = stringResource(
                                        R.string.owes_x,
                                        NumberFormat.getCurrencyInstance().format(debt)
                                    ),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                Method.PERCENTAGES -> {
                    sortedDebtors.map { (member, percentage) ->
                        val debt = groupExpense.amount * (percentage / 100)
                        FriendItem(
                            headline = member.name,
                            hasLeadingContent = false,
                            trailingContent = {
                                Text(
                                    text = stringResource(
                                        R.string.owes_x,
                                        NumberFormat.getCurrencyInstance().format(debt)
                                    ),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                Method.CUSTOM -> {
                    sortedDebtors.map { (member, debt) ->
                        FriendItem(
                            headline = member.name,
                            hasLeadingContent = false,
                            trailingContent = {
                                Text(
                                    text = stringResource(
                                        R.string.owes_x,
                                        NumberFormat.getCurrencyInstance().format(debt)
                                    ),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseDetails(expense: GroupExpense) {
    Text(
        text = NumberFormat.getCurrencyInstance().format(expense.amount),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = stringResource(
            R.string.added_on,
            DateFormat.getDateInstance(DateFormat.LONG)
                .format(Date(expense.addedDate))
        ),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
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

//            Text(
//                stringResource(R.string.split_method) + ": " + stringResource(groupExpense.splitMethod.resId),
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.bodyLarge,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
}