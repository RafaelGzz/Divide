package com.ragl.divide.ui.screens.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ragl.divide.R
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.theme.AppTypography
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    vm: HomeViewModel = hiltViewModel(),
    onAddExpenseClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onSignOut: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    paidExpense: Boolean = false
) {
    val uiState by vm.state.collectAsState()
    val tabs: List<Pair<Int, ImageVector>> = listOf(
        Pair(R.string.bar_item_home_text, Icons.Filled.AttachMoney),
        Pair(R.string.bar_item_friends_text, Icons.Filled.People)
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pullToRefreshState = rememberPullToRefreshState()

    var paidExpenseDialogVisible by remember { mutableStateOf(paidExpense) }

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(top = 20.dp)
        ) {
            if (!uiState.isLoading) {
                if (paidExpenseDialogVisible) {
                    AlertDialog(
                        onDismissRequest = { paidExpenseDialogVisible = false },
                        confirmButton = {
                            TextButton(onClick = { paidExpenseDialogVisible = false }) {
                                Text(text = "OK")
                            }
                        },
                        title = {
                            Text(stringResource(R.string.congratulations))
                        },
                        text = {
                            Text(stringResource(R.string.you_have_paid_your_expense_completely))
                        },
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        textContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
                TopBar(
                    user = uiState.user,
                    onTapUserImage = { vm.signOut { onSignOut() } },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .weight(.1f)
                )
                Box(modifier = Modifier.weight(.8f)) {
                    PullToRefreshBox(
                        isRefreshing = uiState.isLoading,
                        state = pullToRefreshState,
                        indicator = {
                            Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                state = pullToRefreshState,
                                isRefreshing = uiState.isLoading,
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        },
                        onRefresh = { vm.getUserData() }) {
                        when (selectedTabIndex) {
                            0 -> Home(
                                uiState = uiState,
                                onAddExpenseClick = onAddExpenseClick,
                                onAddGroupClick = onAddGroupClick,
                                onExpenseClick = onExpenseClick,
                                onGroupClick = onGroupClick,
                            )

                            1 -> Friends(uiState = uiState)
                        }
                    }
                }
                BottomBar(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(.1f),
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onItemClick = { selectedTabIndex = it }
                )
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
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    tabs: List<Pair<Int, ImageVector>>,
    selectedTabIndex: Int,
    onItemClick: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        HorizontalDivider(thickness = 0.5.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, pair ->
                BottomBarItem(
                    labelStringResource = pair.first,
                    icon = pair.second,
                    selected = selectedTabIndex == index,
                    onItemClick = { onItemClick(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun Friends(uiState: HomeUiState) {
    Column {
        TitleRow(
            labelStringResource = R.string.your_friends,
            buttonStringResource = R.string.add,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp)
        )
    }
}

@Composable
private fun BottomBarItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    @StringRes labelStringResource: Int,
    icon: ImageVector,
    onItemClick: () -> Unit
) {
    Box(modifier = modifier.clickable { onItemClick() }) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(ShapeDefaults.Medium)
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Text(text = stringResource(labelStringResource), fontSize = 14.sp)
        }
    }
}

@Composable
private fun Home(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onAddExpenseClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onGroupClick: (String) -> Unit
) {
    Column(modifier) {
        TitleRow(
            labelStringResource = R.string.your_expenses,
            buttonStringResource = R.string.add,
            onAddClick = onAddExpenseClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp)
        )
        ExpensesRow(
            expenses = uiState.expenses.values.toList().sortedBy { it.id },
            onExpenseClick = { onExpenseClick(it) },
            modifier = Modifier.fillMaxWidth()
        )

        TitleRow(
            labelStringResource = R.string.your_groups,
            buttonStringResource = R.string.add,
            onAddClick = onAddGroupClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp)
        )
        GroupsColumn(
            groups = uiState.groups.values.toList().sortedBy { it.id },
            onGroupClick = { onGroupClick(it) },
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun TitleRow(
    modifier: Modifier = Modifier,
    @StringRes buttonStringResource: Int,
    @StringRes labelStringResource: Int,
    onAddClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(labelStringResource),
            style = MaterialTheme.typography.titleMedium
        )
        Box(modifier = Modifier
            .clip(ShapeDefaults.Small)
            .background(MaterialTheme.colorScheme.primary)
            .wrapContentWidth()
            .clickable { onAddClick() }) {
            Text(
                text = stringResource(buttonStringResource),
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ExpensesRow(
    modifier: Modifier = Modifier,
    expenses: List<Expense>,
    onExpenseClick: (String) -> Unit
) {
    if (expenses.none { !it.paid }) {
        Text(
            text = stringResource(R.string.you_have_no_expenses),
            style = AppTypography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        LazyRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(Modifier.width(8.dp))
            }
            items(expenses.filter { !it.paid }, key = { it.id }) {
                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .clip(ShapeDefaults.Medium)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onExpenseClick(it.id) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        getCategoryIcon(it.category),
                        contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = it.title,
                        style = AppTypography.titleMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Normal),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(12.dp)
                            .widthIn(max = 120.dp)
                    )
                }
            }
//            if (expenses.size > 2) {
//                item {
//                    Row(
//                        modifier = Modifier
//                            .height(80.dp)
//                            .padding(end = 10.dp)
//                            .clip(ShapeDefaults.Medium)
//                            .clickable { },
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Text(
//                            text = stringResource(R.string.see_all),
//                            style = AppTypography.titleMedium.copy(
//                                color = MaterialTheme.colorScheme.primary,
//                                textAlign = TextAlign.Center,
//                                fontSize = 12.sp,
//                                lineHeight = 20.sp
//                            ),
//                            softWrap = true,
//                            overflow = TextOverflow.Ellipsis,
//                            modifier = Modifier.padding(10.dp)
//                        )
//                    }
//                }
//            }
            item {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun GroupsColumn(
    modifier: Modifier = Modifier,
    groups: List<Group>,
    onGroupClick: (String) -> Unit
) {
    if (groups.isEmpty()) Text(
        text = stringResource(R.string.you_have_no_groups),
        style = AppTypography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    ) else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups, key = { it.id }) { group ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeDefaults.Medium)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onGroupClick(group.id) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (group.image.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(group.image)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(100.dp),
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = group.name,
                        style = AppTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    user: User,
    onTapUserImage: () -> Unit
) {
    val owed = 2334.00
    val owe = 122.32
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        Box {
            if (user.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.photoUrl).crossfade(true).build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .size(70.dp)
                        .clickable { onTapUserImage() }
                )
            } else {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .size(70.dp)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .clickable { onTapUserImage() }
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = stringResource(R.string.owed, owed),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal)
            )
            Text(
                text = stringResource(R.string.you_owe, owe),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal)
            )
        }
    }
}