package com.ragl.divide.ui.screens.groupExpenseDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.User
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupExpenseDetailsScreen(
    groupExpenseDetailsViewModel: GroupExpenseDetailsViewModel = hiltViewModel(),
    groupExpense: GroupExpense,
    members: List<User>,
    onEditClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        groupExpenseDetailsViewModel.setGroupExpense(groupExpense)
    }

    val state by groupExpenseDetailsViewModel.groupExpense.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = state.title) },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    scrolledContainerColor = Color.Transparent,
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
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
                        onClick = { onEditClick(state.id) }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { pv ->
        Column(modifier = Modifier
            .padding(pv)
            .padding(horizontal = 16.dp)) {
            Text(
                text = NumberFormat.getCurrencyInstance().format(state.amount),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(
                    R.string.added_on,
                    DateFormat.getDateInstance(DateFormat.LONG)
                        .format(Date(state.addedDate))
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            if (state.notes != "") {
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
                    text = state.notes,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            groupExpense.paidBy.forEach { paidBy ->
                Text(
                    text = members.first { it.uuid == paidBy.key }.name + " paid " + NumberFormat.getCurrencyInstance()
                        .format(paidBy.value),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            groupExpense.debtors.filter { it.value != 0.0 }.forEach { debtor ->
                Text(
                    text = members.first { it.uuid == debtor.key }.name + " owes " + NumberFormat.getCurrencyInstance()
                        .format(debtor.value),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}