package com.ragl.divide.ui.screens.group

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ragl.divide.R
import com.ragl.divide.data.models.Group
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.theme.AppTypography
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.FriendItem
import com.ragl.divide.ui.utils.createImageFile
import java.util.Objects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    vm: GroupViewModel = hiltViewModel(),
    friends: List<User>,
    group: Group,
    members: List<User>,
    isUpdate: Boolean,
    onBackClick: () -> Unit,
    onDeleteGroup: () -> Unit,
    onSaveGroup: (Group) -> Unit,
    user: User,
) {

    LaunchedEffect(Unit) {
        if (isUpdate) {
            vm.setGroup(group, members)
        }
    }

    var isModalSheetVisible by remember { mutableStateOf(false) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            it?.let {
                vm.updateImage(it)
            }
        }
    val context = LocalContext.current

    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context), context.packageName + ".provider", file
    )
    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
            if (it) vm.updateImage(uri)
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            if (it) {
                cameraLauncher.launch(uri)
            }
        }

    val isLoading by vm.isLoading.collectAsState()
    val groupState by vm.group.collectAsState()

    val defaultColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    val selectedColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val selectedFriends = remember { mutableStateListOf<String>() }

    var dialogEnabled by remember { mutableStateOf(false) }
    var isDeleteDialog by remember { mutableStateOf(false) }

    var showFriendSelection by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    BackHandler {
        if (showFriendSelection)
            showFriendSelection = false
        else
            onBackClick()
    }

    Box {
        if (!showFriendSelection) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(stringResource(if (!isUpdate) R.string.add_group else R.string.update_group))
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            navigationIconContentColor = MaterialTheme.colorScheme.primary
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = onBackClick
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!isUpdate && selectedFriends.size > 0)
                        Button(
                            onClick = {
                                vm.saveGroup(onSuccess = onSaveGroup, onError = {
                                    showToast(context, it)
                                })
                            },
                            shape = ShapeDefaults.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.add),
                                style = AppTypography.titleMedium,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                }
            ) { paddingValues ->
                if (isModalSheetVisible) {
                    ModalBottomSheet(
                        onDismissRequest = { isModalSheetVisible = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            stringResource(R.string.select_image_source),
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                        )
                        ListItem(
                            headlineContent = {
                                Text(
                                    stringResource(R.string.select_image_from_gallery),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier.clickable {
                                isModalSheetVisible = false
                                imagePickerLauncher.launch("image/*")
                            }
                        )
                        ListItem(
                            headlineContent = {
                                Text(
                                    stringResource(R.string.take_photo),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier.clickable {
                                isModalSheetVisible = false
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    cameraLauncher.launch(uri)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )
                    }
                } else if (dialogEnabled) {
                    if (isDeleteDialog)
                        AlertDialog(
                            onDismissRequest = { dialogEnabled = false },
                            title = { Text(stringResource(R.string.delete_group), style = MaterialTheme.typography.titleLarge) },
                            text = { Text(stringResource(R.string.delete_group_message), style = MaterialTheme.typography.bodySmall) },
                            confirmButton = {
                                TextButton(onClick = {
                                    dialogEnabled = false
                                    vm.deleteGroup(onDeleteGroup)
                                }) {
                                    Text(stringResource(R.string.delete))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { dialogEnabled = false }) {
                                    Text(stringResource(R.string.cancel))
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                            textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    else
                        AlertDialog(
                            onDismissRequest = { dialogEnabled = false },
                            title = { Text(stringResource(R.string.leave_group), style = MaterialTheme.typography.titleLarge) },
                            text = { Text(stringResource(R.string.leave_group_message), style = MaterialTheme.typography.bodySmall) },
                            confirmButton = {
                                TextButton(onClick = {
                                    dialogEnabled = false
                                    vm.leaveGroup(
                                        onSuccessful = onDeleteGroup,
                                        onError = { showToast(context, it) })
                                }) {
                                    Text(stringResource(R.string.leave))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { dialogEnabled = false }) {
                                    Text(stringResource(R.string.cancel))
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                            textContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                }
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .wrapContentHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { isModalSheetVisible = true }
                                .clip(ShapeDefaults.Medium)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            if (groupState.image.isEmpty() && vm.selectedImageUri == Uri.EMPTY) Icon(
                                Icons.Filled.AddAPhoto,
                                contentDescription = "Add image button",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.Center)
                            )
                            else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(if (vm.selectedImageUri == Uri.EMPTY) groupState.image else vm.selectedImageUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        DivideTextField(
                            label = stringResource(R.string.name),
                            input = groupState.name,
                            error = vm.nameError,
                            onValueChange = { vm.updateName(it) })
                    }
                    Text(
                        text = stringResource(if (!isUpdate) R.string.select_group_members else R.string.group_members),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .wrapContentHeight()
                    )
                    if (!isUpdate)
                        friends.forEach { friend ->
                            val isSelected = selectedFriends.contains(friend.uuid)
                            FriendItem(
                                headline = friend.name,
                                supporting = friend.email,
                                photoUrl = friend.photoUrl,
                                colors = if (isSelected) selectedColors else defaultColors,
                                onClick = {
                                    if (isSelected) {
                                        vm.removeUser(friend.uuid)
                                        selectedFriends.remove(friend.uuid)
                                    } else {
                                        vm.addUser(friend.uuid)
                                        selectedFriends.add(friend.uuid)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    else {
                        vm.members.forEach { member ->
                            FriendItem(
                                headline = member.name,
                                supporting = member.email,
                                photoUrl = member.photoUrl,
                                colors = defaultColors
                            )
                        }
//                        FriendItem(
//                            modifier = Modifier
//                                .padding(vertical = 4.dp),
//                            headline = stringResource(R.string.add_friends_to_group),
//                            colors = selectedColors,
//                            icon = Icons.Filled.GroupAdd,
//                            onClick = { showFriendSelection = true }
//                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFriendSelection = true }
                                .padding(horizontal = 32.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.GroupAdd,
                                "Add friend",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                stringResource(R.string.add_friends_to_group),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = stringResource(R.string.configuration),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .wrapContentHeight()
                        )
                        if (groupState.users[user.uuid]?.totalDebt == 0.0 && groupState.users[user.uuid]?.totalOwed == 0.0 ) {
                            OutlinedButton(
                                onClick = {
                                    isDeleteDialog = false
                                    dialogEnabled = true
                                },
                                shape = ShapeDefaults.Medium,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(
                                    2.dp,
                                    MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = stringResource(R.string.leave_group),
                                    style = AppTypography.titleMedium,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                isDeleteDialog = true
                                dialogEnabled = true
                            },
                            shape = ShapeDefaults.Medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.delete_group),
                                style = AppTypography.titleMedium,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                    if (isUpdate)
                        Button(
                            onClick = {
                                vm.saveGroup(onSuccess = onSaveGroup, onError = {
                                    showToast(context, it)
                                })
                            },
                            shape = ShapeDefaults.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.update),
                                style = AppTypography.titleMedium,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                }

            }
        } else {
            FriendSelectionScreen(
                friends = friends,
                selectedFriends = selectedFriends,
                members = vm.members,
                searchText = searchText,
                onSearchTextChange = { text, filter ->
                    searchText = text
                    filter()
                },
                onFriendClick = { friendId ->
                    if (selectedFriends.contains(friendId)) {
                        selectedFriends.remove(friendId)
                    } else {
                        selectedFriends.add(friendId)
                    }
                },
                onAddClick = {
                    showFriendSelection = false
                    friends.filter { selectedFriends.contains(it.uuid) }.map {
                        vm.addMember(it)
                    }
                    selectedFriends.clear()
                },
                onBackClick = { showFriendSelection = false },
                selectedColors = selectedColors,
                defaultColors = defaultColors
            )
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSelectionScreen(
    friends: List<User>,
    members: List<User>,
    selectedFriends: List<String>,
    searchText: String,
    onSearchTextChange: (String, () -> Unit) -> Unit,
    onFriendClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onBackClick: () -> Unit,
    selectedColors: CardColors,
    defaultColors: CardColors
) {
    var filteredFriends by remember {
        mutableStateOf(friends.filterNot { it.uuid in members.map { m -> m.uuid } })
    }
    var scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.select_friends)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(navigationIconContentColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (selectedFriends.isNotEmpty())
                Button(
                    onClick = onAddClick,
                    shape = ShapeDefaults.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.add), modifier = Modifier.padding(vertical = 16.dp),
                        style = AppTypography.titleMedium,
                    )
                }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
        ) {
            DivideTextField(
                input = searchText,
                onValueChange = {
                    onSearchTextChange(it) {
                        filteredFriends =
                            if (it.isEmpty()) friends else friends.filter { friend ->
                                friend.name.contains(it, ignoreCase = true)
                            }
                    }
                },
                onAction = {
                    filteredFriends = searchText.let {
                        if (it.isEmpty()) friends else friends.filter { friend ->
                            friend.name.contains(it, ignoreCase = true)
                        }
                    }
                },
                imeAction = ImeAction.Search,
                label = stringResource(R.string.search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredFriends, key = { it.uuid }) { friend ->
                    val isSelected = selectedFriends.contains(friend.uuid)
                    val isFriendInGroup = members.find { it.uuid == friend.uuid } != null
                    val friendItemColors = if (isFriendInGroup) {
                        defaultColors
                    } else if (isSelected) {
                        selectedColors
                    } else {
                        defaultColors
                    }
                    if (!isFriendInGroup)
                        FriendItem(
                            headline = friend.name,
                            supporting = friend.email,
                            photoUrl = friend.photoUrl,
                            colors = friendItemColors,
                            onClick = { onFriendClick(friend.uuid) }
                        )
                }
            }
        }
    }
}