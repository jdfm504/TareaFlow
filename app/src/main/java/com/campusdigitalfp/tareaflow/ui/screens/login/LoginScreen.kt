package com.campusdigitalfp.tareaflow.ui.screens.login

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.campusdigitalfp.tareaflow.viewmodel.AuthUiState
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import java.util.regex.Pattern
import com.campusdigitalfp.tareaflow.R

// Pantalla de login para la aplicación
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onGoToRegister: () -> Unit = {}
) {
    // definición de variables
    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val isLoading = uiState is AuthUiState.Loading
    val context = LocalContext.current

    // Validación de email y password
    fun validate(): Boolean {
        var isValid = true
        val emailPattern = Patterns.EMAIL_ADDRESS

        emailError = null
        passwordError = null

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
        } else if (password.length < 6) {
            passwordError = context.getString(R.string.error_password_short)
            isValid = false
        }

        return isValid
    }

    // Diseño de pantalla con surface
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
            // Logo
            Image(
                painter = painterResource(id = R.drawable.list),
                contentDescription = "Logo de la app",
                modifier = Modifier
                    .size(50.dp) // Ajusta el tamaño del logo
                    .padding(bottom = 20.dp)
            )

            // Nombre de la aplicación
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo de email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text(stringResource(R.string.email_label)) },
                singleLine = true,
                isError = emailError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            var passwordVisible by remember { mutableStateOf(false) }

            // Campo del password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_label)) },
                singleLine = true,
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

            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Botón para hacer login
            Button(
                onClick = {
                    if (validate()) {
                        viewModel.login(email, password) { onLoginSuccess() }
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
                Text(
                    if (isLoading)
                        stringResource(R.string.login_loading)
                    else
                        stringResource(R.string.login_button),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Toast
            LaunchedEffect(uiState) {
                when (uiState) {
                    is AuthUiState.Success -> {
                        Toast.makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT)
                            .show()
                        onLoginSuccess()
                    }

                    is AuthUiState.Error -> {
                        Toast.makeText(
                            context,
                            (uiState as AuthUiState.Error).message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }

            // Mostrar barra de carga
            when (uiState) {
                is AuthUiState.Loading -> LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                is AuthUiState.Error -> Text(
                    (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )

                is AuthUiState.Success, AuthUiState.Idle -> {}
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para navegar a pantalla de registro
            TextButton(onClick = { onGoToRegister() }) {
                Text(stringResource
                    (R.string.register_link),
                    color = Color.hsl(123f, 0.40f, 0.45f),
                    fontSize = 14.sp
                )
            }
        }
    }
}