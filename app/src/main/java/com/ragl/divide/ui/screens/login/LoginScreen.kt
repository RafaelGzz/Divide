package com.ragl.divide.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ragl.divide.R
import com.ragl.divide.ui.theme.AppTypography
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onGoogleButtonClick: () -> Unit,
    onLoginButtonClick: (email: String, password: String) -> Unit,
    onSignUpButtonClick: (email: String, password: String, username: String) -> Unit,
    isLoading: Boolean
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(id = R.string.log_in), stringResource(id = R.string.sign_up))
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                }
                HorizontalPager(
                    state = pagerState
                ) { pagerIndex ->
                    when (pagerIndex) {
                        0 -> Login(
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .fillMaxHeight(), onLoginButtonClick, onGoogleButtonClick
                        )

                        1 -> SignUp(
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 20.dp)
                                .verticalScroll(state = rememberScrollState())
                                .fillMaxHeight(), onSignUpButtonClick, onGoogleButtonClick
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(64.dp)
                            .width(64.dp)
                            .align(Alignment.Center),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
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
    modifier: Modifier = Modifier,
    onLoginButtonClick: (String, String) -> Unit,
    onGoogleButtonClick: () -> Unit
) {
    val vm: LoginViewModel = remember { LoginViewModel() }
    Column(
        modifier = modifier
    ) {
        DivideTextField(
            label = stringResource(R.string.email_address_text),
            input = vm.email,
            error = vm.emailError,
            imeAction = ImeAction.Next,
            onValueChange = { vm.updateEmail(it) },
        )
        DivideTextField(
            label = stringResource(R.string.password_text),
            input = vm.password,
            error = vm.passwordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            //isPassword = true,
            onValueChange = { vm.updatePassword(it) },
            onDone = { if (vm.isFieldsValid()) onLoginButtonClick(vm.email, vm.password) }
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
    modifier: Modifier = Modifier,
    onSignUpButtonClick: (String, String, String) -> Unit,
    onGoogleButtonClick: () -> Unit
) {
    val vm: SignupViewModel = remember { SignupViewModel() }
    Column(
        modifier = modifier
    ) {
        DivideTextField(
            label = stringResource(R.string.email_address_text),
            input = vm.email,
            error = vm.emailError,
            imeAction = ImeAction.Next,
            onValueChange = { vm.updateEmail(it) }
        )
        DivideTextField(
            label = stringResource(R.string.username),
            input = vm.username,
            error = vm.usernameError,
            imeAction = ImeAction.Next,
            onValueChange = { vm.updateUsername(it) },
        )
        DivideTextField(
            label = stringResource(R.string.password_text),
            input = vm.password,
            error = vm.passwordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            //isPassword = true,
            onValueChange = { vm.updatePassword(it) },
        )
        DivideTextField(
            label = stringResource(R.string.confirm_password_text),
            input = vm.passwordConfirm,
            error = vm.passwordConfirmError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            //isPassword = true,
            onValueChange = { vm.updatePasswordConfirm(it) },
        )
        LoginButton(
            label = stringResource(R.string.sign_up),
            onClick = {
                if (vm.isFieldsValid()) onSignUpButtonClick(
                    vm.email,
                    vm.password,
                    vm.username
                )
            },
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
        modifier = modifier.clip(ShapeDefaults.Medium)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary
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
fun DivideTextField(
    modifier: Modifier = Modifier,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    label: String,
    input: String,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    errorText: Boolean = true,
    error: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    autoCorrect: Boolean = true,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit = {}
) {
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var icon by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }
    LaunchedEffect(input) {
        if (input.isNotEmpty()) {
            icon = when (keyboardType) {
                KeyboardType.Password -> {
                    {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                }

                KeyboardType.Text -> {
                    {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }

                else -> {
                    null
                }
            }
        } else {
            icon = null
        }
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
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            prefix = prefix,
            suffix = suffix,
            placeholder = placeholder,
            singleLine = singleLine,
            value = input,
            visualTransformation = if (!passwordVisible && keyboardType == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = icon,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = imeAction,
                keyboardType = keyboardType,
                autoCorrectEnabled = autoCorrect
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeDefaults.Medium)
        )
        if (errorText)
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = AppTypography.labelSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )

    }

}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        isLoading = false,
        onGoogleButtonClick = {},
        onSignUpButtonClick = { _, _, _ -> },
        onLoginButtonClick = { _, _ -> },
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Login(
        onLoginButtonClick = { _, _ -> },
        onGoogleButtonClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    SignUp(
        onSignUpButtonClick = { _, _, _ -> },
        onGoogleButtonClick = {}
    )
}