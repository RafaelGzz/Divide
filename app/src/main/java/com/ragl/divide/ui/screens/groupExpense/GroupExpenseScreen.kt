package com.ragl.divide.ui.screens.groupExpense

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.ragl.divide.ui.utils.toTwoDecimals
import com.ragl.divide.ui.utils.validateQuantity
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupExpenseScreen(
    vm: GroupExpenseViewModel = hiltViewModel(),
    group: Group,
    expense: GroupExpense,
    userId: String,
    members: List<User>,
    onBackClick: () -> Unit,
    onSaveExpense: (GroupExpense, GroupExpense) -> Unit
) {
    LaunchedEffect(Unit) {
        vm.setGroupAndExpense(group, userId, members, expense)
    }
    val context = LocalContext.current

    var paidByMenuExpanded by remember { mutableStateOf(false) }
    var methodMenuExpanded by remember { mutableStateOf(false) }

    val sortedMembers = remember {
        members.sortedWith(compareBy({ it.uuid != userId }, { it.name.lowercase() }))
    }
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
                            stringResource(if (!vm.isUpdate.value) R.string.add_expense else R.string.update_expense),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            bottomBar = {

            }
        ) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .imePadding()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                DivideTextField(
                    label = stringResource(R.string.title),
                    input = vm.title,
                    error = vm.titleError,
                    onValueChange = vm::updateTitle,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                )
                DivideTextField(
                    label = stringResource(R.string.amount),
                    keyboardType = KeyboardType.Number,
                    prefix = { Text(text = "$", style = AppTypography.bodyMedium) },
                    input = vm.amount,
                    error = vm.amountError,
                    onValueChange = { input ->
                        validateQuantity(input, vm::updateAmount)
                        vm.updateAmountPerPerson(
                            if (vm.selectedMembers.isEmpty()) 0.0 else {
                                ((vm.amount.toDoubleOrNull()
                                    ?: 0.0) / vm.selectedMembers.size).toTwoDecimals()
                            }
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.paid_by),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
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
                            textStyle = MaterialTheme.typography.bodyMedium,
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
                            sortedMembers.forEach {
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
                AnimatedVisibility(
                    vm.amount.toDoubleOrNull() != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = stringResource(R.string.split_method),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
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
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    onValueChange = {},
                                    singleLine = true,
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = methodMenuExpanded
                                        )
                                    },
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
                                            if (it == Method.EQUALLY) {
                                                vm.updateAmountPerPerson(
                                                    if (vm.selectedMembers.isEmpty()) 0.0 else {
                                                        ((vm.amount.toDoubleOrNull()
                                                            ?: 0.0) / vm.selectedMembers.size).toTwoDecimals()
                                                    }
                                                )
                                            }

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
                            style = AppTypography.labelLarge,
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
                                    Text(
                                        stringResource(
                                            R.string.x_per_person,
                                            NumberFormat.getCurrencyInstance()
                                                .format(vm.amountPerPerson)
                                        ),
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        if (vm.selectedMembers.size == 1) stringResource(
                                            R.string.one_person,
                                            vm.selectedMembers.size
                                        ) else stringResource(
                                            R.string.x_people,
                                            vm.selectedMembers.size
                                        ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Method.PERCENTAGES -> {
                                    val percentageSum = vm.percentages.values.sum()
                                    val remainingPercentage = 100 - percentageSum
                                    val exceeded = percentageSum - 100
                                    Text(
                                        stringResource(R.string.x_of_y, "$percentageSum%", "100%"),
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                    if (percentageSum <= 100.0) {
                                        Text(
                                            stringResource(
                                                R.string.remaining_x,
                                                "$remainingPercentage%"
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    } else {
                                        Text(
                                            stringResource(
                                                R.string.x_exceeded,
                                                "$exceeded%"
                                            ),
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                                        )
                                    }
                                }

                                Method.CUSTOM -> {
                                    val amount = vm.amount.toDoubleOrNull() ?: 0.0
                                    val quantitiesSum = vm.quantities.values.sum()
                                    val remainingQuantity = (amount - quantitiesSum).toTwoDecimals()
                                    val exceeded = (quantitiesSum - amount).toTwoDecimals()
                                    Text(
                                        stringResource(
                                            R.string.x_of_y,
                                            "$$quantitiesSum",
                                            "$$amount"
                                        ),
                                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                                    )
                                    if (quantitiesSum <= amount)
                                        Text(
                                            stringResource(
                                                R.string.remaining_x,
                                                "$$remainingQuantity"
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    else Text(
                                        stringResource(
                                            R.string.x_exceeded,
                                            "$$exceeded"
                                        ),
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        }
                        sortedMembers.forEach { friend ->
                            val friendQuantity = vm.percentages[friend.uuid] ?: 0
                            var percentage by remember { mutableStateOf(friendQuantity.toString()) }
                            var quantity by remember { mutableStateOf(vm.quantities[friend.uuid]!!.toString()) }
                            val amount = vm.amount.toDoubleOrNull() ?: 0.0
                            FriendItem(
                                headline = friend.name,
                                supporting = when (vm.method) {
                                    Method.PERCENTAGES -> "$" + (amount * friendQuantity / 100).toTwoDecimals()

                                    else -> ""
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                trailingContent = {
                                    when (vm.method) {
                                        Method.EQUALLY -> Checkbox(
                                            checked = friend.uuid in vm.selectedMembers,
                                            onCheckedChange = {
                                                vm.updateSelectedMembers(
                                                    if (it) vm.selectedMembers + friend.uuid
                                                    else vm.selectedMembers - friend.uuid
                                                )

                                                vm.updateAmountPerPerson(
                                                    if (vm.selectedMembers.isEmpty()) 0.0 else {
                                                        ((vm.amount.toDoubleOrNull()
                                                            ?: 0.0) / vm.selectedMembers.size).toTwoDecimals()
                                                    }
                                                )
                                            })

                                        Method.PERCENTAGES -> Row(
                                            horizontalArrangement = Arrangement.spacedBy(
                                                8.dp
                                            ), verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BasicTextField(
                                                value = percentage,
                                                onValueChange = {
                                                    if (it.isEmpty()) {
                                                        percentage = ""
                                                        vm.updatePercentages(vm.percentages.mapValues { (key, value) ->
                                                            if (key == friend.uuid) 0
                                                            else value
                                                        })
                                                    } else {
                                                        val input = it.toIntOrNull()
                                                        if (input != null && input in 0..100) {
                                                            percentage = it
                                                            vm.updatePercentages(vm.percentages.mapValues { (key, value) ->
                                                                if (key == friend.uuid) input
                                                                else value
                                                            })
                                                        }
                                                    }
                                                },
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                modifier = Modifier
                                                    .width(60.dp)
                                                    .height(40.dp)
                                                    .clip(ShapeDefaults.Medium)
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(vertical = 10.dp)
                                            )
                                            Text(
                                                text = "%",
                                                style = AppTypography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                                            )
                                        }

                                        Method.CUSTOM -> Row(
                                            horizontalArrangement = Arrangement.spacedBy(
                                                8.dp
                                            ), verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$",
                                                style = AppTypography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                                            )
                                            BasicTextField(
                                                value = quantity,
                                                onValueChange = {
                                                    validateQuantity(it) { res ->
                                                        quantity = res
                                                        vm.updateQuantities(vm.quantities.mapValues { (key, value) ->
                                                            if (key == friend.uuid) if (res.isEmpty()) 0.0 else res.toDouble()
                                                            else value
                                                        })
                                                    }
                                                },
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                modifier = Modifier
                                                    .width(60.dp)
                                                    .height(40.dp)
                                                    .clip(ShapeDefaults.Medium)
                                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                                    .padding(vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        Button(
                            onClick = {
                                if (vm.validateTitle().and(vm.validateAmount())) {
                                    when (vm.method) {
                                        Method.EQUALLY -> {
                                            if (vm.selectedMembers.size < 2) {
                                                showToast(
                                                    context,
                                                    context.getString(R.string.two_people_must_be_selected)
                                                )
                                                return@Button
                                            }
                                        }

                                        Method.PERCENTAGES -> {
                                            if (vm.percentages.values.sum() != 100) {
                                                showToast(
                                                    context,
                                                    context.getString(R.string.percentages_sum_must_be_100)
                                                )
                                                return@Button
                                            } else if (vm.percentages.values.any { it == 100 }) {
                                                showToast(
                                                    context,
                                                    context.getString(R.string.two_people_must_pay)
                                                )
                                                return@Button
                                            }
                                        }

                                        Method.CUSTOM -> {
                                            if (vm.quantities.values.any { it == vm.amount.toDouble() }) {
                                                showToast(
                                                    context,
                                                    context.getString(R.string.two_people_must_pay)
                                                )
                                                return@Button
                                            } else if (vm.quantities.values.sum() != (vm.amount.toDouble())) {
                                                showToast(
                                                    context,
                                                    context.getString(
                                                        R.string.quantities_sum_must_be_amount,
                                                        vm.amount
                                                    )
                                                )
                                                return@Button
                                            }
                                        }
                                    }
                                    vm.saveExpense(
                                        onSuccess = onSaveExpense,
                                        onError = { showToast(context, it) }
                                    )
                                }
                            },
                            shape = ShapeDefaults.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(if (!vm.isUpdate.value) R.string.add else R.string.update),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
