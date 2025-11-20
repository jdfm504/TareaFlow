package com.campusdigitalfp.tareaflow.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.campusdigitalfp.tareaflow.viewmodel.AuthUiState
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import java.util.regex.Pattern

// Pantalla de login para la aplicación
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onGoToRegister: () -> Unit = {}
) {
    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
        )
        emailError = null
        passwordError = null

        if (email.isBlank()) {
            emailError = "Introduce tu correo electrónico"
            isValid = false
        } else if (!emailPattern.matcher(email).matches()) {
            emailError = "Formato de correo no válido"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "Introduce tu contraseña"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        return isValid
    }

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
                text = "TareaFlow",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = emailError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = passwordError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError != null) {
                Text(
                    text = passwordError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validate()) {
                        viewModel.login(email, password) { onLoginSuccess() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }

            // Debajo, muestra feedback:
            when (uiState) {
                is AuthUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                is AuthUiState.Error -> Text(
                    (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                is AuthUiState.Success, AuthUiState.Idle -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { onGoToRegister() }) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}


