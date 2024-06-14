package com.ragl.divide.ui.screens.group

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
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
import com.ragl.divide.ui.showToast
import com.ragl.divide.ui.theme.AppTypography
import com.ragl.divide.ui.utils.DivideTextField
import com.ragl.divide.ui.utils.createImageFile
import java.util.Objects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    vm: GroupViewModel = hiltViewModel(),
    groupId: String,
    onBackClick: () -> Unit,
    onAddGroup: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (groupId.isNotEmpty()) vm.setGroup(groupId)
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
            vm.updateImage(uri)
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
            if (it) {
                cameraLauncher.launch(uri)
            }
        }

    val isLoading by vm.isLoading.collectAsState()
    val group by vm.state.collectAsState()

    Box {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(stringResource(if (groupId.isEmpty()) R.string.add_group else R.string.update_group))
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
                        vm.saveGroup(onSuccess = { onAddGroup() }, onError = {
                            showToast(context, it)
                        })
                    },
                    shape = ShapeDefaults.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp)
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                ) {
                    Text(
                        text = stringResource(if (groupId.isEmpty()) R.string.add else R.string.update),
                        style = AppTypography.titleMedium
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
                        })
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
                        })
                }
            }
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                DivideTextField(
                    label = stringResource(R.string.name),
                    input = group.name,
                    error = vm.nameError,
                    onValueChange = { vm.updateName(it) })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clickable { isModalSheetVisible = true }
                        .clip(ShapeDefaults.Medium)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    if (group.image.isEmpty() && vm.selectedImageUri == Uri.EMPTY) Icon(
                        Icons.Filled.AddAPhoto,
                        contentDescription = "Add image button",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    )
                    else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(if (vm.selectedImageUri == Uri.EMPTY) group.image else vm.selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
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