package com.ragl.divide.ui.screens.groupExpense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
                .padding(horizontal = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DivideTextField(
                    label = stringResource(R.string.title),
                    input = vm.title,
                    error = vm.titleError,
                    onValueChange = vm::updateTitle,
                    modifier = Modifier.weight(.5f)
                )
                DivideTextField(
                    label = stringResource(R.string.amount),
                    keyboardType = KeyboardType.Number,
                    prefix = { Text(text = "$", style = AppTypography.bodyLarge) },
                    input = vm.amount,
                    error = vm.amountError,
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
                    modifier = Modifier.weight(.5f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(
                    modifier = Modifier.weight(.5f)
                ) {
                    Text(
                        text = stringResource(R.string.paid_by),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        modifier = Modifier.fillMaxWidth(),
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
                Column(
                    modifier = Modifier.weight(.5f)
                ) {
                    Text(
                        text = stringResource(R.string.divided_by),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        modifier = Modifier.fillMaxWidth(),
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
                                        text = stringResource(it.resId),
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
            }

        }
    }
}