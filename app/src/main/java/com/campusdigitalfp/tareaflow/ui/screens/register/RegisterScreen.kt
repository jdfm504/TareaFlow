package com.campusdigitalfp.tareaflow.ui.screens.register

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusdigitalfp.tareaflow.viewmodel.AuthUiState
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import java.util.regex.Pattern
import com.campusdigitalfp.tareaflow.R

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onGoToLogin: () -> Unit = {}
) {
    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.register_success),
                    Toast.LENGTH_SHORT
                ).show()
                onRegisterSuccess()
            }

            is AuthUiState.Error -> {
                val id = (uiState as AuthUiState.Error).messageRes
                Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val isLoading = uiState is AuthUiState.Loading

    // Validaciones básicas
    fun validate(): Boolean {
        var isValid = true
        val emailPattern = Patterns.EMAIL_ADDRESS
        val passwordPattern = Pattern.compile(
            "^(?=.*[a-z])" +                       // al menos una minúscula
                    "(?=.*[A-Z])" +                       // al menos una mayúscula
                    "(?=.*\\d)" +                         // al menos un número
                    "(?=.*[@\$!%*?&.,;:#^(){}\\[\\]_\\-+=~`|<>\\\\'\"/])" +  // al menos un carácter especial
                    "[A-Za-z\\d@\$!%*?&.,;:#^(){}\\[\\]_\\-+=~`|<>\\\\'\"/]{6,}$" // mínimo 6 caracteres
        )

        nameError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null

        if (name.isBlank()) {
            nameError = context.getString(R.string.error_name_empty)
            isValid = false
        }

        if (email.isBlank()) {
            emailError = context.getString(R.string.error_email_empty)
            isValid = false
        } else if (!emailPattern.matcher(email).matches()) {
            emailError = context.getString(R.string.error_email_format)
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = context.getString(R.string.error_password_empty)
            isValid = false
        } else if (!passwordPattern.matcher(password).matches()) {
            passwordError = context.getString(R.string.error_password_format)
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = context.getString(R.string.error_confirm_empty)
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = context.getString(R.string.error_confirm_mismatch)
            isValid = false
        }

        return isValid
    }

    // Ui de la pantalla
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.register_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo Nombre
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text(stringResource(R.string.register_name_label)) },
                singleLine = true,
                isError = nameError != null,
                modifier = Modifier.fillMaxWidth()
            )
            nameError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text(stringResource(R.string.register_email_label)) },
                singleLine = true,
                isError = emailError != null,
                modifier = Modifier.fillMaxWidth()
            )
            emailError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            var passwordVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text(stringResource(R.string.register_password_label)) },
                singleLine = true,
                isError = passwordError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image =
                        if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible)
                        stringResource(R.string.hide_password)
                    else
                        stringResource(R.string.show_password)
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )
            passwordError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Confirmar contraseña
            var confirmVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = { Text(stringResource(R.string.register_confirm_password_label)) },
                singleLine = true,
                isError = confirmPasswordError != null,
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image =
                        if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description =
                        if (confirmVisible)
                            stringResource(R.string.hide_password)
                        else
                            stringResource(R.string.show_password)
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )
            confirmPasswordError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Botón de registro
            Button(
                onClick = {
                    if (validate()) {
                        viewModel.register(email, password) { onRegisterSuccess() }
                    }
                },
                enabled = !isLoading, // desactiva cuando está cargando
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isLoading) 0.6f else 1f) // efecto visual de desactivado
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(if (isLoading)
                    stringResource(R.string.register_loading)
                else
                    stringResource(R.string.register_button),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when (uiState) {
                is AuthUiState.Loading -> LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                is AuthUiState.Error -> {
                    val id = (uiState as AuthUiState.Error).messageRes
                    Text(
                        text = stringResource(id),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                is AuthUiState.Success, AuthUiState.Idle -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { onGoToLogin() }) {
                Text(stringResource
                    (R.string.register_to_login_link),
                    color = Color.hsl(123f, 0.40f, 0.45f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
