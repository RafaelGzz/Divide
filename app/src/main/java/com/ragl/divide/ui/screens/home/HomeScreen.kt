package com.ragl.divide.ui.screens.home

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ragl.divide.R
import com.ragl.divide.data.models.Expense
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.data.models.getCategoryIcon
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.theme.AppTypography
import com.ragl.divide.ui.utils.FriendItem
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    vm: UserViewModel,
    onAddExpenseClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onSignOut: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onAddFriendsClick: () -> Unit,
    onChangeDarkMode: (String?) -> Unit,
    isDarkMode: String?
) {
    val tabs: List<Pair<Int, ImageVector>> = listOf(
        Pair(R.string.bar_item_home_text, Icons.Filled.AttachMoney),
        Pair(R.string.bar_item_friends_text, Icons.Filled.People),
        Pair(R.string.bar_item_profile_text, Icons.Filled.Person)
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val pullToRefreshState = rememberPullToRefreshState()

    val user by vm.user.collectAsState()
    val friends = remember(user.friends) {
        user.friends.values.toList().sortedBy { it.name.lowercase() }
    }
    val expenses = remember(user.expenses) {
        user.expenses.values.toList().sortedBy { it.id }
    }
    val groups = remember(user.groups) {
        user.groups.values.toList().sortedBy { it.id }
    }
    var pullLoading by remember { mutableStateOf(user.isLoading) }
    LaunchedEffect(user.isLoading) {
        pullLoading = user.isLoading
    }

    var exit by remember { mutableStateOf(false) }
    val context = LocalContext.current
    BackHandler {
        if (exit) {
            context.startActivity(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } else {
            exit = true
            showToast(context, context.getString(R.string.press_again_to_exit))
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (user.isLoading) {
            CircularProgressIndicator()
        }
        AnimatedVisibility(
            visible = !user.isLoading,
            enter = fadeIn(animationSpec = tween(500)),
            exit = ExitTransition.None
        ) {
            Scaffold(
                bottomBar = {
                    BottomBar(
                        tabs = tabs,
                        selectedTabIndex = selectedTabIndex,
                        onItemClick = {
                            selectedTabIndex = it
                        }
                    )
                },
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = selectedTabIndex == 1,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = ExitTransition.None
                    ) {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.add_friends)) },
                            icon = { Icon(Icons.Filled.GroupAdd, contentDescription = null) },
                            onClick = onAddFriendsClick,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = modifier
                        .padding(paddingValues)
                ) {
                    PullToRefreshBox(
                        isRefreshing = pullLoading,
                        state = pullToRefreshState,
                        onRefresh = vm::getUserData,
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                state = pullToRefreshState,
                                isRefreshing = pullLoading,
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            AnimatedVisibility(
                                visible = selectedTabIndex == 0,
                                enter = fadeIn(animationSpec = tween(500)),
                                exit = ExitTransition.None
                            ) {
                                HomeBody(
                                    expenses = expenses,
                                    groups = groups,
                                    onAddExpenseClick = onAddExpenseClick,
                                    onAddGroupClick = onAddGroupClick,
                                    onExpenseClick = onExpenseClick,
                                    onGroupClick = onGroupClick
                                )
                            }
                            AnimatedVisibility(
                                visible = selectedTabIndex == 1,
                                enter = fadeIn(animationSpec = tween(500)),
                                exit = ExitTransition.None
                            ) {
                                FriendsBody(friends = friends)
                            }
                            AnimatedVisibility(
                                visible = selectedTabIndex == 2,
                                enter = fadeIn(animationSpec = tween(500)),
                                exit = ExitTransition.None
                            ) {
                                ProfileBody(
                                    user = user.user,
                                    onSignOut = { vm.signOut { onSignOut() } },
                                    isDarkMode = isDarkMode,
                                    onChangeDarkMode = onChangeDarkMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileBody(
    modifier: Modifier = Modifier,
    user: User,
    onSignOut: () -> Unit,
    isDarkMode: String?,
    onChangeDarkMode: (String?) -> Unit
) {
    val allowNotifications = remember { mutableStateOf(true) }
    var isSignOutDialogVisible by remember { mutableStateOf(false) }
    Column {
        if (isSignOutDialogVisible) {
            AlertDialog(
                onDismissRequest = { isSignOutDialogVisible = false },
                confirmButton = {
                    TextButton(onClick = onSignOut) {
                        Text(stringResource(R.string.yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isSignOutDialogVisible = false }) {
                        Text(stringResource(R.string.no))
                    }
                },
                title = {
                    Text(stringResource(R.string.sign_out), style = MaterialTheme.typography.titleLarge)
                },
                text = {
                    Text(stringResource(R.string.sign_out_confirmation), style = MaterialTheme.typography.bodySmall)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
                textContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        TopBar(
            title = stringResource(R.string.bar_item_profile_text)
        )
        Box(modifier = modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (user.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photoUrl).crossfade(true).build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(52.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .padding(12.dp)
                            .clip(CircleShape)
                    )
                }
                Column {
                    Text(
                        user.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        user.email,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { isSignOutDialogVisible = true }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NotificationSetting(allowNotifications)
        Spacer(modifier = Modifier.height(16.dp))
        DarkModeSetting(isDarkMode, onChangeDarkMode)
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NotificationSetting(allowNotifications: MutableState<Boolean>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { allowNotifications.value = !allowNotifications.value }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.allow_notifications),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = allowNotifications.value, onCheckedChange = { allowNotifications.value = it })
        }
    }
}

@Composable
private fun DarkModeSetting(
    isDarkMode: String?,
    onChangeDarkMode: (String?) -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { isExpanded.value = !isExpanded.value },
    ) {
        Row(
            Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = if (isExpanded.value) 0.dp else 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.dark_mode),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                Icon(
                    if (isExpanded.value) Icons.Filled.KeyboardArrowUp
                    else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
        AnimatedVisibility(visible = isExpanded.value) {
            Column {
                Row(
                    Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(36.dp))
                    Text(
                        stringResource(R.string.activated),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = isDarkMode == "true",
                        onClick = { onChangeDarkMode("true") })
                }
                Row(
                    Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(36.dp))
                    Text(
                        stringResource(R.string.deactivated),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(
                        selected = isDarkMode == "false",
                        onClick = { onChangeDarkMode("false") })
                }
                Row(
                    Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(36.dp))
                    Text(
                        stringResource(R.string.system_default),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RadioButton(selected = isDarkMode == null, onClick = { onChangeDarkMode(null) })
                }
            }
        }
    }
}

@Composable
fun FriendsBody(
    friends: List<User>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                TopBar(
                    title = stringResource(R.string.bar_item_friends_text)
                )
            }
            if (friends.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.you_have_no_friends),
                        style = AppTypography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else items(friends, key = { it.uuid }) { friend ->
                FriendItem(
                    headline = friend.name,
                    supporting = friend.email,
                    photoUrl = friend.photoUrl,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

        }
    }
}

@Composable
private fun HomeBody(
    expenses: List<Expense>,
    groups: List<Group>,
    onAddExpenseClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onExpenseClick: (String) -> Unit,
    onGroupClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TopBar(
                title = stringResource(R.string.app_name)
            )
        }
        item {
            TitleRow(
                labelStringResource = R.string.your_expenses,
                buttonStringResource = R.string.add,
                onAddClick = onAddExpenseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
            )

            ExpensesRow(
                expenses = expenses,
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
        }
        if (groups.isEmpty())
            item {
                Text(
                    text = stringResource(R.string.you_have_no_groups),
                    style = AppTypography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        else items(groups, key = { it.id }) { group ->
            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
                    .clip(ShapeDefaults.Medium)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onGroupClick(group.id) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (group.image.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(group.image)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.Companion
                            .size(100.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.Companion
                            .size(100.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text = group.name,
                    style = AppTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier.Companion
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
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
                    .padding(horizontal = 28.dp, vertical = 8.dp)
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
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .widthIn(max = 120.dp)
                    ) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Normal
                            ),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = NumberFormat.getCurrencyInstance().format(it.amount),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Normal
                            ),
                            softWrap = true,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String
) {
    Box(
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }

}

@Composable
private fun BottomBar(
    tabs: List<Pair<Int, ImageVector>>,
    selectedTabIndex: Int,
    onItemClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp)
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
                tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.6f
                ),
                modifier = Modifier
                    .clip(ShapeDefaults.Medium)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Text(
                text = stringResource(labelStringResource),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.6f
                    )
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}