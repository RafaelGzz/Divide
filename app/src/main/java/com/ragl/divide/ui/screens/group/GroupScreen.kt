package com.ragl.divide.ui.screens.group

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    isUpdate: Boolean,
    onBackClick: () -> Unit,
    onDeleteGroup: () -> Unit,
    onAddGroup: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (isUpdate) {
            vm.setGroup(group)
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

    Box {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(if (!isUpdate) R.string.add_group else R.string.update_group))
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
                        vm.saveGroup(onSuccess = onAddGroup, onError = {
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
                        text = stringResource(if (!isUpdate) R.string.add else R.string.update),
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
                    ListItem(
                        headlineContent = { Text("Select image from gallery") },
                        modifier = Modifier.clickable {
                            isModalSheetVisible = false
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Take photo") },
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
                        title = { Text(stringResource(R.string.delete_group)) },
                        text = { Text(stringResource(R.string.delete_group_message)) },
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
                        title = { Text(stringResource(R.string.leave_group)) },
                        text = { Text(stringResource(R.string.leave_group_message)) },
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
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .wrapContentHeight()
                )
                val height = if (isUpdate) 320.dp else 200.dp
                if (!isUpdate)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                    ) {
                        items(friends, key = { it.uuid }) { friend ->
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
                        }
                    }
                else {
                    FriendItem(
                        modifier = Modifier
                            .padding(vertical = 4.dp),
                        headline = stringResource(R.string.add_friends_to_group),
                        icon = Icons.Filled.GroupAdd
                    )
                    vm.members.forEach {member ->
                        FriendItem(
                            modifier = Modifier.padding(vertical = 4.dp),
                            headline = member.name,
                            supporting = member.email,
                            photoUrl = member.photoUrl,
                            colors = selectedColors
                        )
                    }
                    Text(
                        text = stringResource(R.string.configuration),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .wrapContentHeight()
                    )
                    if (groupState.users.size != 1) {
                        OutlinedButton(
                            onClick = {
                                isDeleteDialog = false
                                dialogEnabled = true
                            },
                            shape = ShapeDefaults.Medium,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.errorContainer
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
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
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

            }

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