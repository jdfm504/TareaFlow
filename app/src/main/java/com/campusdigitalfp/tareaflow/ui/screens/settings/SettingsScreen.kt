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
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusdigitalfp.tareaflow.R
import com.campusdigitalfp.tareaflow.ui.theme.ApplyStatusBarTheme
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import com.campusdigitalfp.tareaflow.viewmodel.UserProfileViewModel
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlin.text.clear
import kotlin.toString

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
    var tempShortBreak by rememberSaveable { mutableStateOf(prefs.shortBreakMinutes.toString()) }
    var tempLongBreak by rememberSaveable { mutableStateOf(prefs.longBreakMinutes.toString()) }
    var tempCycles by rememberSaveable { mutableStateOf(prefs.cyclesUntilLongBreak.toString()) }

    var tempName by rememberSaveable { mutableStateOf("") }

    val isAnonymous = FirebaseAuth.getInstance().currentUser?.isAnonymous == true

    // Cargar nombre al entrar
    LaunchedEffect(profile.name) {
        if (tempName.isEmpty()) tempName = profile.name
    }
    // Cargar pomodoro al entrar
    LaunchedEffect(
        prefs.pomodoroMinutes,
        prefs.shortBreakMinutes,
        prefs.longBreakMinutes,
        prefs.cyclesUntilLongBreak
    )
    {
        tempPomodoro = prefs.pomodoroMinutes.toString()
        tempShortBreak = prefs.shortBreakMinutes.toString()
        tempLongBreak = prefs.longBreakMinutes.toString()
        tempCycles = prefs.cyclesUntilLongBreak.toString()
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
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnonymous   // desactivado si es anónimo
        )
        if (isAnonymous) {
            Text(
                text = stringResource(R.string.settings_name_disabled),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { navController.navigate("register") },
                fontWeight = FontWeight.SemiBold
            )
        }

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

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = tempPomodoro,
            onValueChange = { nuevo ->
                if (nuevo.all { it.isDigit() }) {  // solo números
                    if (nuevo.isBlank()) {
                        tempPomodoro = ""          // permitir borrar temporalmente
                    } else {
                        val n = nuevo.toInt()
                        if (n >= 1) tempPomodoro = nuevo  // impedir 0
                    }
                }
            },
            label = { Text(stringResource(R.string.settings_pomodoro_focus)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = tempShortBreak,
            onValueChange = { nuevo ->
                if (nuevo.all { it.isDigit() }) {
                    if (nuevo.isBlank()) {
                        tempShortBreak = ""
                    } else {
                        val n = nuevo.toInt()
                        if (n >= 1) tempShortBreak = nuevo
                    }
                }
            },
            label = { Text(stringResource(R.string.settings_pomodoro_short_break)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = tempLongBreak,
            onValueChange = { nuevo ->
                if (nuevo.all { it.isDigit() }) {
                    if (nuevo.isBlank()) {
                        tempLongBreak = ""
                    } else {
                        val n = nuevo.toInt()
                        if (n >= 1) tempLongBreak = nuevo
                    }
                }
            },
            label = { Text(stringResource(R.string.settings_pomodoro_long_break)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = tempCycles,
            onValueChange = { nuevo ->
                if (nuevo.all { it.isDigit() }) {
                    if (nuevo.isBlank()) {
                        tempCycles = ""
                    } else {
                        val n = nuevo.toInt()
                        if (n >= 1) tempCycles = nuevo
                    }
                }
            },
            label = { Text(stringResource(R.string.settings_pomodoro_cycles)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        SoftDivider()

        // ======================= CONTRASEÑA =======================
        SectionHeader(
            title = stringResource(R.string.settings_section_password),
            description = stringResource(R.string.settings_section_password_desc)
        )

        if (isAnonymous) {
            Text(
                text = stringResource(R.string.settings_password_disabled),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(20.dp)
                    )
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
        }

        Spacer(Modifier.height(40.dp))

        // ======================= BOTÓN GUARDAR CAMBIOS    =======================

        val authViewModel: AuthViewModel = viewModel()

        // Función para guardar los cambios de perfil si no se requiere cambio de contraseña y controlando que no sea un anonimo el que intenta realizar cambios
        fun guardarCambiosPerfil(): Boolean {
            var cambios = false

            val isAnonymous = FirebaseAuth.getInstance().currentUser?.isAnonymous == true

            // ----- cambio de nombre -----
            if (!isAnonymous) {
                if (tempName.isNotBlank() && tempName != profile.name) {
                    profileViewModel.updateName(tempName)
                    cambios = true
                }
            } else {
                // Si el usuario es anónimo NO guardar nombre
                // Pero permitimos pomodoro
            }

            //  cambio de tiempo pomodoro permitido para todos
            // Validar que el tiempo es >1 siempre
            fun safeInt(value: String, defaultValue: Int): Int {
                val n = value.toIntOrNull()
                return if (n == null || n < 1) defaultValue else n
            }

            val p = safeInt(tempPomodoro, prefs.pomodoroMinutes)
            if (p != prefs.pomodoroMinutes) {
                prefsViewModel.setPomodoro(p)
                cambios = true
            }

            val sb = safeInt(tempShortBreak, prefs.shortBreakMinutes)
            if (sb != prefs.shortBreakMinutes) {
                prefsViewModel.setShortBreak(sb)
                cambios = true
            }

            val lb = safeInt(tempLongBreak, prefs.longBreakMinutes)
            if (lb != prefs.longBreakMinutes) {
                prefsViewModel.setLongBreak(lb)
                cambios = true
            }

            val cycles = safeInt(tempCycles, prefs.cyclesUntilLongBreak)
            if (cycles != prefs.cyclesUntilLongBreak) {
                prefsViewModel.setCycles(cycles)
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
                val isAnonymous = FirebaseAuth.getInstance().currentUser?.isAnonymous == true

                // Si es anónimo no puede cambiar contraseña ni nombre
                if (isAnonymous) {
                    val cambios = guardarCambiosPerfil()

                    if (cambios) {
                        Toast.makeText(context, R.string.profile_saved, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.no_changes, Toast.LENGTH_SHORT).show()
                    }
                    return@Button
                }

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
                val authViewModel: AuthViewModel = viewModel()
                val profileViewModel: UserProfileViewModel = viewModel()
                val prefsViewModel: PreferencesViewModel = viewModel()

                TextButton(onClick = {
                    showDeleteDialog = false

                    authViewModel.deleteAccount { success, messageRes ->
                        if (!success) {
                            Toast.makeText(
                                context,
                                context.getString(messageRes!!),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                R.string.account_deleted,
                                Toast.LENGTH_LONG
                            ).show()

                            FirebaseAuth.getInstance().signOut()
                            profileViewModel.clear()
                            prefsViewModel.clear()

                            navController.navigate("onboarding") {
                                popUpTo(0)
                            }
                        }
                    }
                }) {
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
