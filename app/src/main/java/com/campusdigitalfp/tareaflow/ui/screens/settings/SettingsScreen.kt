package com.campusdigitalfp.tareaflow.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusdigitalfp.tareaflow.R
import com.campusdigitalfp.tareaflow.ui.theme.ApplyStatusBarTheme
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import com.campusdigitalfp.tareaflow.viewmodel.UserProfileViewModel
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    profileViewModel: UserProfileViewModel,
    prefsViewModel: PreferencesViewModel = viewModel()
) {
    val prefs by prefsViewModel.prefs.collectAsState()
    val context = LocalContext.current
    val profile by profileViewModel.profile.collectAsState()
    val focusManager = LocalFocusManager.current

    ApplyStatusBarTheme()

    // Estados locales
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    var tempPomodoro by rememberSaveable { mutableStateOf(prefs.pomodoroMinutes.toString()) }
    var tempName by rememberSaveable { mutableStateOf("") }

    // Cargar nombre al entrar
    LaunchedEffect(profile.name) {
        if (tempName.isEmpty()) tempName = profile.name
    }
    // Cargar pomodoro al entrar
    LaunchedEffect(prefs.pomodoroMinutes) {
        tempPomodoro = prefs.pomodoroMinutes.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {

        // ------------------ TITULO ------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )

        // ======================= NOMBRE USUARIO =======================
        SectionHeader(
            title = stringResource(R.string.settings_profile_title),
            description = stringResource(R.string.settings_profile_desc)
        )

        OutlinedTextField(
            value = tempName,
            onValueChange = { tempName = it },
            label = { Text(stringResource(R.string.settings_profile_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        SoftDivider()

        // ======================= APARIENCIA =======================
        SectionHeader(
            title = stringResource(R.string.settings_section_appearance),
            description = stringResource(R.string.settings_section_appearance_desc)
        )

        SimpleRow(
            title = stringResource(R.string.settings_dark_theme),
            trailing = {
                ModernSwitch(
                    checked = prefs.darkTheme,
                    onCheckedChange = { prefsViewModel.setDarkTheme(it) }
                )
            }
        )

        SoftDivider()

        // ======================= POMODORO =======================
        SectionHeader(
            title = stringResource(R.string.settings_section_pomodoro),
            description = stringResource(R.string.settings_section_pomodoro_desc)
        )

        OutlinedTextField(
            value = tempPomodoro,
            onValueChange = { v ->
                if (v.all { it.isDigit() }) {
                    tempPomodoro = v
                }
            },
            singleLine = true,
            modifier = Modifier.width(120.dp),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        SoftDivider()

        // ======================= CONTRASEÑA =======================
        SectionHeader(
            title = stringResource(R.string.settings_section_password),
            description = stringResource(R.string.settings_section_password_desc)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text(stringResource(R.string.settings_password_current)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(stringResource(R.string.settings_password_new)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                label = { Text(stringResource(R.string.settings_password_repeat)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(40.dp))

        // ======================= BOTÓN GUARDAR CAMBIOS    =======================

        val authViewModel: AuthViewModel = viewModel()

        // Función para guardar los cambios de perfil si no se requiere cambio de contraseña
        fun guardarCambiosPerfil(): Boolean {
            var cambios = false

            if (tempName.isNotBlank() && tempName != profile.name) {
                profileViewModel.updateName(tempName)
                cambios = true
            }

            val p = tempPomodoro.toIntOrNull()
            if (p != null && p != prefs.pomodoroMinutes) {
                prefsViewModel.setPomodoro(p)
                cambios = true
            }

            // Si hay cambios quitar el foco del cuadro de texto
            if (cambios) {
                focusManager.clearFocus()
            }

            return cambios
        }

        // Botón para guardar los cambios
        Button(
            onClick = {

                val quiereCambiarPassword =
                    currentPassword.isNotBlank() &&
                            newPassword.isNotBlank() &&
                            repeatPassword.isNotBlank()

                if (quiereCambiarPassword) {

                    if (newPassword != repeatPassword) {
                        Toast.makeText(context, R.string.error_confirm_mismatch, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    authViewModel.changePassword(
                        currentPassword,
                        newPassword
                    ) { success, messageRes ->

                        if (!success) {
                            Toast.makeText(context, context.getString(messageRes!!), Toast.LENGTH_LONG).show()
                            return@changePassword
                        }

                        // Contraseña cambiada
                        Toast.makeText(context, R.string.password_updated, Toast.LENGTH_SHORT).show()

                        currentPassword = ""
                        newPassword = ""
                        repeatPassword = ""

                        //quitar foco del cuadro de texto
                        focusManager.clearFocus()

                        // Guardar cambios de nombre y pomodoro
                        val otrosCambios = guardarCambiosPerfil()
                    }
                    return@Button
                }

                // guardar nombre y pomodoro aunque no se cambie contraseña
                val cambios = guardarCambiosPerfil()

                if (cambios) {
                    Toast.makeText(context, R.string.profile_saved, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.no_changes, Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp)
        )
        {
            Text(
                text = stringResource(R.string.save_button),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))


        // ======================= BOTÓN BORRAR CUENTA =======================
        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_delete_account),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(60.dp))

    }

    // ------------------ DIÁLOGO ------------------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_title),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_message),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = stringResource(R.string.dialog_yes_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = stringResource(R.string.dialog_cancel),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

// ======================= COMPONENTES =======================
// Para las cabeceras de los apartados
@Composable
fun SectionHeader(title: String, description: String) {
    Column(
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SimpleRow(
    title: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier.padding(end = 4.dp)
        ) {
            trailing()
        }
    }
}

@Composable
fun SoftDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 24.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// Switch para cambiar de tema
@Composable
fun ModernSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Switch(checked = checked, onCheckedChange = onCheckedChange, colors = colors)
}
