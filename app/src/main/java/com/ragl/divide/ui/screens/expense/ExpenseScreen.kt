package com.ragl.divide.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.Category
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Frequency
import com.ragl.divide.data.services.ScheduleNotificationService
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.theme.AppTypography
import com.ragl.divide.ui.utils.DivideTextField
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    vm: ExpenseViewModel = hiltViewModel(),
    expense: Expense,
    isUpdate: Boolean = false,
    onBackClick: () -> Unit,
    onSaveExpense: (Expense) -> Unit
) {
    LaunchedEffect(Unit) {
        if (isUpdate) {
            vm.setViewModelExpense(expense)
        }
    }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var frequencyMenuExpanded by remember { mutableStateOf(false) }
    var paymentSuffix by remember { mutableIntStateOf(R.string.payments) }

    var reminderPermissionMessageDialogEnabled by remember { mutableStateOf(false) }

    var selectDateDialogEnabled by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val scheduleNotificationService = ScheduleNotificationService(context)

    LaunchedEffect(vm.payments) {
        paymentSuffix = if (vm.payments == "1") R.string.payments else R.string.payments_plural
    }
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
                        scheduleNotificationService = scheduleNotificationService,
                        onSuccess = onSaveExpense,
                        onError = {
                            showToast(context, it)
                        }
                    )
                },
                shape = ShapeDefaults.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .padding(horizontal = 16.dp, vertical = 32.dp)
            ) {
                Text(
                    text = stringResource(if (!isUpdate) R.string.add else R.string.update),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        }
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectDateDialogEnabled) {
                FrequencyStartingDatePicker(
                    time = vm.startingDate,
                    onDismissRequest = { selectDateDialogEnabled = false },
                    onConfirmClick = { vm.updateStartingDate(it) }
                )
            }
            if (reminderPermissionMessageDialogEnabled) {
                AlertDialog(
                    onDismissRequest = { reminderPermissionMessageDialogEnabled = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                reminderPermissionMessageDialogEnabled = false
                                scheduleNotificationService.requestScheduleExactAlarmPermission()
                            }
                        ) { Text(stringResource(R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { reminderPermissionMessageDialogEnabled = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.reminder_permission_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.reminder_permission_message),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(state = scrollState)
                    .fillMaxSize()
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DivideTextField(
                        label = stringResource(R.string.title),
                        input = vm.title,
                        error = vm.titleError,
                        onValueChange = { vm.updateTitle(it) },
                        modifier = Modifier.weight(.55f)
                    )
                    Column(
                        Modifier.weight(.45f)
                    ) {
                        Text(
                            text = stringResource(R.string.category),
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Normal),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = categoryMenuExpanded,
                            onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }) {
                            TextField(
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                value = vm.category.name,
                                onValueChange = {},
                                singleLine = true,
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .clip(ShapeDefaults.Medium)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryMenuExpanded,
                                onDismissRequest = { categoryMenuExpanded = false },
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                                    .clip(CircleShape)
                            ) {
                                Category.entries.forEach {
                                    DropdownMenuItem(text = {
                                        Text(
                                            text = it.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }, onClick = {
                                        vm.updateCategory(it)
                                        categoryMenuExpanded = false
                                    },
                                        modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DivideTextField(
                        label = stringResource(R.string.amount),
                        keyboardType = KeyboardType.Number,
                        prefix = { Text(text = "$", style = AppTypography.bodyLarge) },
                        input = vm.amount,
                        error = vm.amountError,
                        enabled = vm.amountPaid == 0.0,
                        onValueChange = { input ->
                            if (input.isEmpty()) vm.updateAmount("") else {
                                val formatted = input.replace(",", ".")
                                val parsed = formatted.toDoubleOrNull()
                                parsed?.let {
                                    val decimalPart = formatted.substringAfter(".", "")
                                    if (decimalPart.length <= 2 && parsed <= 999999999.99) {
                                        vm.updateAmount(input)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(.55f)
                    )
                    DivideTextField(
                        label = stringResource(R.string.divided_in),
                        input = vm.payments,
                        error = vm.paymentsError,
                        keyboardType = KeyboardType.NumberPassword,
                        suffix = {
                            Text(
                                stringResource(paymentSuffix),
                                style = AppTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        },
                        onValueChange = { if (it.isDigitsOnly()) vm.updatePayments(it) },
                        modifier = Modifier.weight(.45f)
                    )
                }
                DivideTextField(
                    label = stringResource(R.string.notes),
                    input = vm.notes,
                    onValueChange = { vm.updateNotes(it) },
                    imeAction = ImeAction.Default,
                    singleLine = false,
                    errorText = false,
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .padding(bottom = 12.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 16.dp)
                ) {
                    Checkbox(
                        checked = vm.isRemindersEnabled,
                        onCheckedChange = {
                            if (it) {
                                if (!scheduleNotificationService.canScheduleExactAlarms()) {
                                    reminderPermissionMessageDialogEnabled = true
                                } else {
                                    vm.updateIsRemindersEnabled(true)
                                }
                            } else {
                                vm.updateIsRemindersEnabled(false)
                            }
                        },
                        colors = CheckboxDefaults.colors(uncheckedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = stringResource(R.string.get_reminders),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                if (vm.isRemindersEnabled) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            Modifier.weight(.55f)
                        ) {
                            Text(
                                text = stringResource(R.string.frequency),
                                style = AppTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = frequencyMenuExpanded,
                                onExpandedChange = {
                                    frequencyMenuExpanded = !frequencyMenuExpanded
                                }) {
                                TextField(
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    value = stringResource(vm.frequency.resId),
                                    onValueChange = {},
                                    singleLine = true,
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = frequencyMenuExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                                        .clip(ShapeDefaults.Medium)
                                )
                                ExposedDropdownMenu(
                                    expanded = frequencyMenuExpanded,
                                    onDismissRequest = { frequencyMenuExpanded = false },
                                    modifier = Modifier
                                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Frequency.entries.forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    stringResource(it.resId),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            onClick = {
                                                vm.updateFrequency(it)
                                                frequencyMenuExpanded = false
                                            },
                                            modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            Modifier.weight(.45f)
                        ) {
                            Text(
                                text = stringResource(R.string.starting_from),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(
                                onClick = { selectDateDialogEnabled = true },
                                shape = ShapeDefaults.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.select_date),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyStartingDatePicker(
    onDismissRequest: () -> Unit,
    onConfirmClick: (Long) -> Unit,
    time: Long
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = time)
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                    onConfirmClick(state.selectedDateMillis ?: Date().time)
                }
            ) { Text(text = stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}