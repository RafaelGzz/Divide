package com.ragl.divide.ui.screens.addFriends

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.utils.DivideTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendsScreen(
    vm: AddFriendsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onFriendAdded: (User) -> Unit,
    friends: List<User>
) {
    BackHandler {
        onBackClick()
    }
    LaunchedEffect(Unit) {
        vm.setCurrentFriends(friends)
    }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.add_friends),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showAddFriendDialog) {
            AlertDialog(
                onDismissRequest = { showAddFriendDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                iconContentColor = MaterialTheme.colorScheme.onSurface,
                confirmButton = {
                    TextButton(onClick = {
                        showAddFriendDialog = false
                        vm.addFriend(onFriendAdded)
                    }) {
                        Text(stringResource(R.string.add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFriendDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = { Text(stringResource(R.string.add_friend), style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(stringResource(R.string.do_you_want_to_add, vm.selectedUser!!.name), style = MaterialTheme.typography.bodySmall)
                }
            )
        }
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DivideTextField(
                    label = stringResource(R.string.search_by_email),
                    input = vm.searchText,
                    onValueChange = vm::updateSearchText,
                    imeAction = ImeAction.Done,
                    onAction = vm::searchUser,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = vm::searchUser,
                    modifier = Modifier
                        .wrapContentSize()
                        .height(55.dp),
                    shape = ShapeDefaults.Medium
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Add Friend")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (vm.users.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.no_users_found),
                            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                items(vm.users.values.toList().sortedBy { it.name }) { user ->
                    UserItem(user = user, onUserClick = {
                        showAddFriendDialog = true
                        vm.updateSelectedUser(it)
                    })
                }
            }
        }
    }
}

@Composable
fun UserItem(user: User, onUserClick: (User) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onUserClick(user) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
            )
            Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}