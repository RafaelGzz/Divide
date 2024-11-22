package com.ragl.divide.ui.screens.groupPaymentDetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupUser
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.theme.AppTypography
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.validateQuantity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPaymentDetailsScreen(
    vm: GroupPaymentDetailsViewModel = hiltViewModel(),
    payment: Payment,
    onBackClick: () -> Unit,
    members: List<User>,
    group: Group,
    groupUser: GroupUser
) {
    LaunchedEffect(Unit) {
        vm.setGroupAndPayment(group, members, payment, groupUser)
    }
    BackHandler {
        onBackClick()
    }
    var owedMenuExpanded by remember { mutableStateOf(false) }
    var groupUser = vm.groupUser.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(if (!vm.isUpdate) R.string.make_a_payment else R.string.update_payment),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.who_will_you_pay),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            ExposedDropdownMenuBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                expanded = owedMenuExpanded,
                onExpandedChange = { owedMenuExpanded = !owedMenuExpanded }) {
                TextField(
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    value = vm.selectedOwedUser.name,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    onValueChange = {},
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = owedMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .clip(ShapeDefaults.Medium)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = owedMenuExpanded,
                    onDismissRequest = { owedMenuExpanded = false },
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    vm.owedUsers.forEach {
                        DropdownMenuItem(text = {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }, onClick = {
                            vm.updateSelectedOwedUser(it)
                            owedMenuExpanded = false
                        },
                            modifier = Modifier.background(color = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }
            }
            Text(
                "¿Cuánto deseas pagar?",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                "Le debes $${groupUser.value.debts[vm.selectedOwedUser.uuid]} a ${vm.selectedOwedUser.name}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.6f
                    )
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            DivideTextField(
                label = stringResource(R.string.amount),
                keyboardType = KeyboardType.Number,
                prefix = { Text(text = "$", style = AppTypography.bodyMedium) },
                input = vm.amount,
                error = vm.amountError,
                onValueChange = { input ->
                    validateQuantity(input, vm::updateAmount)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
            )
            Button(
                onClick = {
                    vm.makePayment()
                },
                shape = ShapeDefaults.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Realizar pago",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.weight(2f))
        }
    }
}