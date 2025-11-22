package com.campusdigitalfp.tareaflow.ui.screens.register

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

    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, "Registro correcto", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, (uiState as AuthUiState.Error).message, Toast.LENGTH_SHORT).show()
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
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
        )
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

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Repite la contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }

    // UI de la pantalla
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

            // Campo Nombre
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
                label = { Text("Correo electrónico") },
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
                label = { Text("Contraseña") },
                singleLine = true,
                isError = passwordError != null,
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
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                isError = confirmPasswordError != null,
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image = if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (confirmVisible) "Ocultar contraseña" else "Mostrar contraseña"
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

            Spacer(modifier = Modifier.height(24.dp))

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
                Text(if (isLoading) "Registrando..." else "Registrarse")
            }

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

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { onGoToLogin() }) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
