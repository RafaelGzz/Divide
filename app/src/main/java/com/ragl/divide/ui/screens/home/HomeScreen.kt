package com.ragl.divide.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ragl.divide.R
import com.ragl.divide.data.models.User
import com.ragl.divide.ui.theme.AppTypography

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    vm: HomeViewModel = hiltViewModel(),
    user: User,
    onSignOut: () -> Unit
) {
    Scaffold {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Top
        ) {
            TopBar(
                user = user,
                onTapUserImage = { vm.signOut { onSignOut() } }
            )
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                ExpenseButtonRow()
                Text(text = "Your expenses", style = AppTypography.titleMedium, modifier = Modifier.padding(vertical =  20.dp))
            }
        }
    }
}

@Composable
private fun ExpenseButtonRow(
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        ExpenseButton(
            modifier = Modifier.weight(1f),
            label = "Individual\nExpense"
        )
        ExpenseButton(
            modifier = Modifier.weight(1f),
            label = "Group\nExpense"
        )
    }
}

@Composable
private fun ExpenseButton(
    modifier: Modifier = Modifier,
    label: String
) {
    Button(
        onClick = { /*TODO*/ },
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(0.dp),
        shape = ShapeDefaults.Medium,
        modifier = modifier
            .height(80.dp)
    ) {
        Icon(
            Icons.Outlined.CurrencyExchange,
            contentDescription = null
        )
        Text(text = label, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun TopBar(
    modifier: Modifier = Modifier,
    user: User,
    onTapUserImage: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                if (user.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photoUrl).crossfade(true).build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                            .clickable { onTapUserImage() }
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(32.dp)
                            .clickable { onTapUserImage() }
                    )
                }
            }
            Text(
                text = stringResource(R.string.app_name),
                style = AppTypography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Spacer(
                modifier = Modifier
                    .width(40.dp)
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = .08f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}