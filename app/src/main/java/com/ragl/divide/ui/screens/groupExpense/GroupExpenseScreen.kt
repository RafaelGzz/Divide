package com.ragl.divide.ui.screens.groupExpense

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Method
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.theme.AppTypography
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.validateQuantity
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupExpenseScreen(
    vm: GroupExpenseViewModel = hiltViewModel(),
    group: Group,
    userId: String,
    members: List<User>,
    isUpdate: Boolean = false,
    onBackClick: () -> Unit,
    onSaveExpense: (GroupExpense) -> Unit
) {
    LaunchedEffect(Unit) {
        vm.setGroup(group, userId, members)
    }
    val context = LocalContext.current

    var paidByMenuExpanded by remember { mutableStateOf(false) }
    var methodMenuExpanded by remember { mutableStateOf(false) }

    var selectedFriends by remember { mutableStateOf(members.map { it.uuid }) }
    var amountPerPerson by remember { mutableDoubleStateOf(0.0) }

    BackHandler {
        if (methodMenuExpanded) methodMenuExpanded = false
        else onBackClick()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(if (!isUpdate) R.string.add_expense else R.string.update_expense),
                            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                Button(
                    onClick = {
                        vm.saveExpense(
                            onSuccess = onSaveExpense,
                            onError = {
                                showToast(context, it)
                            }
                        )
                    },
                    shape = ShapeDefaults.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(if (!isUpdate) R.string.add else R.string.update),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {

                DivideTextField(
                    label = stringResource(R.string.title),
                    input = vm.title,
                    error = vm.titleError,
                    onValueChange = vm::updateTitle,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                DivideTextField(
                    label = stringResource(R.string.amount),
                    keyboardType = KeyboardType.Number,
                    prefix = { Text(text = "$", style = AppTypography.bodyLarge) },
                    input = vm.amount,
                    error = vm.amountError,
                    onValueChange = { input ->
                        validateQuantity(input, vm::updateAmount)
                        amountPerPerson = (vm.amount.toDoubleOrNull() ?: 0.0) / selectedFriends.size
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.paid_by),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        expanded = paidByMenuExpanded,
                        onExpandedChange = { paidByMenuExpanded = !paidByMenuExpanded }) {
                        TextField(
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            value = vm.paidBy.name,
                            onValueChange = {},
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paidByMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .clip(ShapeDefaults.Medium)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = paidByMenuExpanded,
                            onDismissRequest = { paidByMenuExpanded = false },
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            vm.members.forEach {
                                DropdownMenuItem(text = {
                                    Text(
                                        text = it.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }, onClick = {
                                    vm.updatePaidBy(it)
                                    paidByMenuExpanded = false
                                },
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.split_method),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        expanded = methodMenuExpanded,
                        onExpandedChange = { methodMenuExpanded = !methodMenuExpanded }) {
                        TextField(
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            value = stringResource(vm.method.resId),
                            onValueChange = {},
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodMenuExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .clip(ShapeDefaults.Medium)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = methodMenuExpanded,
                            onDismissRequest = { methodMenuExpanded = false },
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Method.entries.forEach {
                                DropdownMenuItem(text = {
                                    Text(
                                        stringResource(it.resId),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }, onClick = {
                                    vm.updateMethod(it)
                                    methodMenuExpanded = false
                                },
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = when (vm.method) {
                        Method.EQUALLY -> stringResource(R.string.select_who_pays)
                        Method.PERCENTAGES -> stringResource(R.string.indicate_percentages)
                        Method.CUSTOM -> stringResource(R.string.indicate_quantities)
                    },
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(horizontal = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    when (vm.method) {
                        Method.EQUALLY -> {
                            Text("${NumberFormat.getCurrencyInstance().format(amountPerPerson)}/persona",
                                style = AppTypography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                "(${selectedFriends.size} ${if (selectedFriends.size > 1) "personas" else "persona"})",
                                style = AppTypography.bodySmall
                            )
                        }

                        Method.PERCENTAGES -> {

                        }

                        Method.CUSTOM -> {

                        }
                    }
                }
                vm.members.forEach { friend ->
                    var percentage by remember { mutableStateOf("0") }
                    var quantity by remember { mutableStateOf("") }

                    FriendItem(
                        headline = friend.name,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        trailingContent = {
                            when (vm.method) {
                                Method.EQUALLY -> Checkbox(
                                    checked = friend.uuid in selectedFriends,
                                    onCheckedChange = {
                                        selectedFriends = if (it) selectedFriends + friend.uuid
                                        else selectedFriends - friend.uuid

                                        amountPerPerson = (vm.amount.toDoubleOrNull()
                                            ?: 0.0) / selectedFriends.size
                                    })

                                Method.PERCENTAGES -> TextField(
                                    value = percentage,
                                    onValueChange = { percentage = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = ShapeDefaults.Medium,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Percent,
                                            contentDescription = "Percentage icon",
                                            Modifier.size(16.dp)
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    modifier = Modifier.width(80.dp)
                                )

                                Method.CUSTOM -> TextField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = ShapeDefaults.Medium,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.AttachMoney,
                                            contentDescription = "Money icon",
                                            Modifier.size(16.dp)
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    modifier = Modifier.width(80.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}