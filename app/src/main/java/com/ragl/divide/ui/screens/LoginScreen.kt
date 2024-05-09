package com.ragl.divide.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ragl.divide.R
import com.ragl.divide.ui.theme.AppTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    vm: LoginViewModel = hiltViewModel(),
    onSuccess: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(id = R.string.log_in), stringResource(id = R.string.sign_up))
    val pagerState = rememberPagerState { tabs.size }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = AppTypography.headlineLarge.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            selectedTabIndex = index
                        },
                        text = { Text(text = title) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.height(64.dp)
                    )
                }
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
        HorizontalPager(
            state = pagerState
        ) { pagerIndex ->
            when (pagerIndex) {
                0 -> Login(
                    email = vm.email,
                    emailError = vm.emailError,
                    password = vm.password,
                    passwordError = vm.passwordError,
                    onEmailChange = { vm.updateEmail(it) },
                    onPasswordChange = { vm.updatePassword(it) },
                    onButtonClicked = {
                        vm.tryLogin(
                            onSuccessfulLogin = onSuccess,
                            onFailedLogin = {
                                Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )

                1 -> SignUp(
                    email = vm.email,
                    emailError = vm.emailError,
                    password = vm.password,
                    passwordError = vm.passwordError,
                    onEmailChange = { vm.updateEmail(it) },
                    onPasswordChange = { vm.updatePassword(it) },
                    onButtonClicked = {
                        vm.tryLogin(
                            onSuccessfulLogin = onSuccess,
                            onFailedLogin = {
                                Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    })
            }
        }
    }
}

@Composable
fun SignUp(
    email: String,
    emailError: String,
    password: String,
    passwordError: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onButtonClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .fillMaxHeight()
    ) {
        LoginTextField(
            label = stringResource(R.string.email_address_text),
            input = email,
            error = emailError,
            onValueChange = { onEmailChange(it) },
        )
        LoginTextField(
            label = stringResource(R.string.password_text),
            input = password,
            error = passwordError,
            isPassword = true,
            onValueChange = { onPasswordChange(it) },
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
        )
        LoginButton(label = stringResource(R.string.sign_up), onClick = { onButtonClicked() })
        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.connect_with_social_media),
            style = AppTypography.titleMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(R.string.connect_with_google),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .clip(ShapeDefaults.ExtraLarge)
                    .clickable { }
            )
            Spacer(modifier = Modifier.width(32.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_facebook),
                contentDescription = stringResource(R.string.connect_with_facebook),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .clip(ShapeDefaults.ExtraLarge)
                    .clickable { }
            )
        }
    }
}

@Composable
private fun Login(
    email: String,
    emailError: String,
    password: String,
    passwordError: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onButtonClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 32.dp)
            .fillMaxHeight()
    ) {
        LoginTextField(
            label = stringResource(R.string.email_address_text),
            input = email,
            error = emailError,
            onValueChange = { onEmailChange(it) },
        )
        LoginTextField(
            label = stringResource(R.string.password_text),
            input = password,
            error = passwordError,
            isPassword = true,
            onValueChange = { onPasswordChange(it) },
            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
        )
        LoginButton(label = stringResource(R.string.log_in), onClick = { onButtonClicked() })
        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.connect_with_social_media),
            style = AppTypography.titleMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(R.string.connect_with_google),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .clip(ShapeDefaults.ExtraLarge)
                    .clickable { }
            )
            Spacer(modifier = Modifier.width(32.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_facebook),
                contentDescription = stringResource(R.string.connect_with_facebook),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .clip(ShapeDefaults.ExtraLarge)
                    .clickable { }
            )
        }
    }
}

@Composable
private fun LoginButton(onClick: () -> Unit, label: String) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = ShapeDefaults.ExtraSmall,
        modifier = Modifier.shadow(4.dp, shape = ShapeDefaults.Medium)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun LoginTextField(
    label: String,
    input: String,
    error: String = "",
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
            value = input,
            visualTransformation = if (!passwordVisible && isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (input.isNotEmpty()) {
                    if (isPassword) {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        // Please provide localized description for accessibility services
                        val description =
                            if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    } else {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = if (isPassword) ImeAction.Done else ImeAction.Next,
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                autoCorrect = !isPassword
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = ShapeDefaults.Medium)
        )
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = AppTypography.labelSmall,
            modifier = Modifier.padding(vertical = 4.dp)
        )

    }

}