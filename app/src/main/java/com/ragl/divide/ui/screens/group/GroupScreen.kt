package com.ragl.divide.ui.screens.group

import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ragl.divide.R
import com.ragl.divide.ui.theme.AppTypography
import com.ragl.divide.ui.utils.DivideTextField

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
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            vm.updateImage(it)
        }

    val context = LocalContext.current
    val state by vm.state.collectAsState()

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
                        Toast.makeText(
                            context, it, Toast.LENGTH_SHORT
                        ).show()
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            DivideTextField(
                label = stringResource(R.string.name),
                input = state.name,
                error = vm.nameError,
                onValueChange = { vm.updateName(it) })
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clickable { launcher.launch("image/*") }
                    .clip(ShapeDefaults.Medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                if (vm.selectedImageUri == null) Icon(
                    Icons.Filled.AddAPhoto,
                    contentDescription = "Add image button",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                )
                else {
                    AsyncImage(
                        model = vm.selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}