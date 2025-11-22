package com.campusdigitalfp.tareaflow.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusdigitalfp.tareaflow.viewmodel.AuthUiState
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import java.util.regex.Pattern

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onGoToLogin: () -> Unit = {}
) {
    val viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
        )
        val passwordPattern = Pattern.compile(
            "^(?=.*[a-z])" +                       // al menos una minúscula
                    "(?=.*[A-Z])" +                       // al menos una mayúscula
                    "(?=.*\\d)" +                         // al menos un número
                    "(?=.*[@\$!%*?&.,;:#^(){}\\[\\]_\\-+=~`|<>\\\\'\"/])" +  // al menos un carácter especial
                    "[A-Za-z\\d@\$!%*?&.,;:#^(){}\\[\\]_\\-+=~`|<>\\\\'\"/]{6,}$" // caracteres permitidos (mínimo 6)
        )

        nameError = null
        emailError = null
        passwordError = null

        if (name.isBlank()) {
            nameError = "Introduce tu nombre"
            isValid = false
        }

        if (email.isBlank()) {
            emailError = "Introduce tu correo electrónico"
            isValid = false
        } else if (!emailPattern.matcher(email).matches()) {
            emailError = "Formato de correo no válido"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "Introduce una contraseña"
            isValid = false
        } else if (!passwordPattern.matcher(password).matches()) {
            passwordError =
                "Debe tener 6+ caracteres, incluir mayúscula, minúscula, número y símbolo"
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
                text = "Crear cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Nombre completo") },
                singleLine = true,
                isError = nameError != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError != null) {
                Text(
                    text = nameError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            var passwordVisible by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

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
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validate()) {
                        viewModel.register(email, password) { onRegisterSuccess() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
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

            TextButton(onClick = { onGoToLogin() }) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
