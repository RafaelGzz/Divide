package com.ragl.divide.ui.screens.groupDetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ragl.divide.R
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.GroupExpense
import com.ragl.divide.data.models.Payment
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.theme.AppTypography
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupDetailsViewModel: GroupDetailsViewModel = hiltViewModel(),
    group: Group,
    userId: String,
    members: List<User>,
    editGroup: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
) {
    BackHandler {
        onBackClick()
    }
    LaunchedEffect(Unit) {
        groupDetailsViewModel.setGroup(group, userId, members)
    }
    val groupState by groupDetailsViewModel.group.collectAsState()
    val isLoading by groupDetailsViewModel.isLoading.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            if (!isLoading)
                GroupDetailsAppBar(
                    groupName = groupState.name,
                    image = groupState.image,
                    onBackClick = onBackClick,
                    scrollBehavior = scrollBehavior,
                    onEditClick = {
                        editGroup(groupState.id)
                    }
                )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        stringResource(R.string.add_expense),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                icon = { Icon(Icons.Filled.AttachMoney, contentDescription = null) },
                onClick = onAddExpenseClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp),
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        if (!isLoading) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                if (groupState.expenses.isEmpty()) {
                    Text(
                        text = stringResource(R.string.group_no_expenses),
                        style = AppTypography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                ExpenseListView(
                    expensesAndPayments = groupDetailsViewModel.expensesAndPayments,
                    modifier = Modifier.weight(1f),
                    getPaidByNames = groupDetailsViewModel::getPaidByNames,
                    members = members
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ExpenseListView(
    modifier: Modifier = Modifier,
    expensesAndPayments: List<Any>,
    getPaidByNames: (List<String>) -> String,
    members: List<User>
) {
    val expensesByMonth = expensesAndPayments.groupBy {
        when (it) {
            is GroupExpense -> {
                SimpleDateFormat(
                    "MMMM yyyy",
                    Locale.getDefault()
                ).format(it.addedDate)
            }

            is Payment -> {
                SimpleDateFormat(
                    "MMMM yyyy",
                    Locale.getDefault()
                ).format(it.date)
            }

            else -> ""
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(expensesByMonth.keys.toList().sortedDescending()) { month ->
            MonthSection(
                month = month,
                expensesAndPayments = expensesByMonth[month] ?: emptyList(),
                getPaidByNames = getPaidByNames,
                members = members
            )
        }
    }
}

@Composable
private fun MonthSection(
    month: String,
    expensesAndPayments: List<Any>,
    getPaidByNames: (List<String>) -> String,
    members: List<User>
) {
    Column {
        Text(
            text = month,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (expense in expensesAndPayments.sortedByDescending {
                when (it) {
                    is GroupExpense -> it.addedDate
                    is Payment -> it.date
                    else -> null
                }
            }) {
                GroupExpenseItem(
                    expenseOrPayment = expense,
                    getPaidByNames = getPaidByNames,
                    members = members
                )
            }
        }
    }
}

@Composable
private fun GroupExpenseItem(
    expenseOrPayment: Any,
    getPaidByNames: (List<String>) -> String,
    members: List<User>
) {
    val dateFormatter = SimpleDateFormat("MMM\ndd", Locale.getDefault())
    val formattedDate = dateFormatter.format(
        when (expenseOrPayment) {
            is GroupExpense -> expenseOrPayment.addedDate
            is Payment -> expenseOrPayment.date
            else -> null
        }
    )
    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .clip(ShapeDefaults.Medium)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            formattedDate,
            style = AppTypography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(start = 16.dp)
        )
        Icon(
            if (expenseOrPayment is GroupExpense) getCategoryIcon(expenseOrPayment.category) else Icons.Filled.AttachMoney,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            contentDescription = null,
            modifier = Modifier.padding(start = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = 12.dp), verticalArrangement = Arrangement.Center
        ) {
            when (expenseOrPayment) {
                is GroupExpense -> {
                    Text(
                        text = expenseOrPayment.title,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Normal
                        ),
                    )
                    Text(
                        text = stringResource(R.string.paid_by) + " " + getPaidByNames(
                            expenseOrPayment.paidBy.values.toList()
                        ),
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Normal
                        ),
                    )
                }

                is Payment -> {
                    Text(
                        "${members.find { it.uuid == expenseOrPayment.paidBy }?.name} paid ${members.find { it.uuid == expenseOrPayment.paidTo }?.name}",
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Normal
                        ),
                    )
                }
            }
        }
        Text(
            text = NumberFormat.getCurrencyInstance().format(
                when (expenseOrPayment) {
                    is GroupExpense -> expenseOrPayment.amount
                    is Payment -> expenseOrPayment.amount
                    else -> 0
                }
            ),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            ),
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.padding(end = 20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailsAppBar(
    groupName: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    image: String,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        collapsedHeight = 80.dp,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            scrolledContainerColor = Color.Transparent,
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (image.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(image)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(60.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    groupName,
                    softWrap = true,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
                Icon(Icons.Filled.Settings, contentDescription = "Edit")
            }
        }
    )
}