package com.ragl.divide.ui.screens.groupDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ragl.divide.R
import com.ragl.divide.ui.screens.home.TitleRow
import com.ragl.divide.ui.theme.AppTypography

@Composable
fun GroupDetailsScreen(
    groupDetailsViewModel: GroupDetailsViewModel = hiltViewModel(),
    groupId: String,
    editGroup: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddExpense: () -> Unit
) {

    LaunchedEffect(Unit) {
        groupDetailsViewModel.setGroup(groupId)
    }

    val context = LocalContext.current
    val group by groupDetailsViewModel.group.collectAsState()
    val isLoading by groupDetailsViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            if (!isLoading)
                GroupDetailsAppBar(
                    groupName = group.name,
                    onBackClick = onBackClick,
                    onEditClick = {
                        editGroup(groupId)
                    }
                )
        }
    ) { paddingValues ->
        if (!isLoading) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                TitleRow(
                    buttonStringResource = R.string.add_expense,
                    labelStringResource = R.string.group_expenses,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
                if (group.expenses.isEmpty()) {
                    Text(
                        text = stringResource(R.string.group_no_expenses),
                        style = AppTypography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(group.expenses.values.toList(), key = { it.id }){
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ShapeDefaults.Medium)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {  },
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailsAppBar(
    groupName: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                groupName,
                softWrap = true,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp)
            )
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
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
        }
    )
}