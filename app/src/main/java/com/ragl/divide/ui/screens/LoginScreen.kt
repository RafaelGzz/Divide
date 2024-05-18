package com.ragl.divide.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ragl.divide.R
import com.ragl.divide.ui.theme.AppTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onGoogleButtonClick: () -> Unit,
    onLoginButtonClick: (email: String, password: String) -> Unit,
    onSignUpButtonClick: (email: String, password: String, username: String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(id = R.string.log_in), stringResource(id = R.string.sign_up))
    val pagerState = rememberPagerState { tabs.size }
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
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = AppTypography.headlineLarge.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
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
                0 -> Login(onLoginButtonClick, onGoogleButtonClick)
                1 -> SignUp(onSignUpButtonClick, onGoogleButtonClick)
            }
        }
    }
}

@Composable
private fun SocialMediaRow(
    onGoogleButtonClick: () -> Unit
) {
    Row {
        Icon(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = stringResource(R.string.connect_with_google),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(36.dp)
                .clip(ShapeDefaults.ExtraLarge)
                .clickable {
                    onGoogleButtonClick()
                }
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

@Composable
private fun Login(
    onLoginButtonClick: (String, String) -> Unit,
    onGoogleButtonClick: () -> Unit
) {
    val vm: LoginViewModel = remember { LoginViewModel() }
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .fillMaxHeight()
    ) {
        LoginTextField(
            label = stringResource(R.string.email_address_text),
            input = vm.email,
            error = vm.emailError,
            onValueChange = { vm.updateEmail(it) },
        )
        LoginTextField(
            label = stringResource(R.string.password_text),
            input = vm.password,
            error = vm.passwordError,
            isPassword = true,
            onValueChange = { vm.updatePassword(it) },
        )
        LoginButton(
            label = stringResource(R.string.log_in),
            onClick = { if (vm.isFieldsValid()) onLoginButtonClick(vm.email, vm.password) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.connect_with_social_media),
            style = AppTypography.titleMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        SocialMediaRow(onGoogleButtonClick)
    }
}

@Composable
fun SignUp(
    onSignUpButtonClick: (String, String, String) -> Unit,
    onGoogleButtonClick: () -> Unit
) {
    val vm: SignupViewModel = remember {SignupViewModel() }
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(state = rememberScrollState())
            .fillMaxHeight()
    ) {
        LoginTextField(
            label = stringResource(R.string.email_address_text),
            input = vm.email,
            error = vm.emailError,
            onValueChange = { vm.updateEmail(it) },
            modifier = Modifier.padding(top = 20.dp)
        )
        LoginTextField(
            label = stringResource(R.string.username),
            input = vm.username,
            error = vm.usernameError,
            onValueChange = { vm.updateUsername(it) },
        )
        LoginTextField(
            label = stringResource(R.string.password_text),
            input = vm.password,
            error = vm.passwordError,
            isPassword = true,
            onValueChange = { vm.updatePassword(it) },
        )
        LoginTextField(
            label = stringResource(R.string.confirm_password_text),
            input = vm.passwordConfirm,
            error = vm.passwordConfirmError,
            isPassword = true,
            onValueChange = { vm.updatePasswordConfirm(it) },
        )
        LoginButton(
            label = stringResource(R.string.sign_up),
            onClick = { if (vm.isFieldsValid()) onSignUpButtonClick(vm.email, vm.password, vm.username) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.connect_with_social_media),
            style = AppTypography.titleMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        SocialMediaRow(onGoogleButtonClick)
    }
}


@Composable
private fun LoginButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String
) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = ShapeDefaults.ExtraSmall,
        modifier = modifier.shadow(4.dp, shape = ShapeDefaults.Medium)
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
    modifier: Modifier = Modifier,
    label: String,
    input: String,
    error: String = "",
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    onDone: () -> Unit = {}
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