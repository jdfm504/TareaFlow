package com.campusdigitalfp.tareaflow.ui.screens.settings

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusdigitalfp.tareaflow.MainActivity
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.campusdigitalfp.tareaflow.ui.theme.GreenPrimary
import com.campusdigitalfp.tareaflow.R
import com.campusdigitalfp.tareaflow.ui.theme.ApplyStatusBarTheme
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    prefsViewModel: PreferencesViewModel = viewModel()
) {
    val prefs by prefsViewModel.prefs.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var pendingLanguage by rememberSaveable { mutableStateOf<String?>(null) }


    // Ajustar la status bar según el tema
    val systemUiController = rememberSystemUiController()

    ApplyStatusBarTheme()

    // Estados locales
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }


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

        Divider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp)
        )

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
            value = prefs.pomodoroMinutes.toString(),
            onValueChange = { v ->
                if (v.all { it.isDigit() } && v.isNotBlank()) {
                    prefsViewModel.setPomodoro(v.toInt())
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
    // --- Diálogo para cambiar idioma (requiere reinicio) ---
    if (showLanguageDialog && pendingLanguage != null) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = stringResource(R.string.settings_language_dialog_title)) },
            text = { Text(stringResource(R.string.settings_language_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val lang = pendingLanguage!!

                        // 1) Guardar en Firestore
                        prefsViewModel.setLanguage(lang)

                        // 2) Aplicar locales
                        val locales = LocaleListCompat.forLanguageTags(lang)
                        AppCompatDelegate.setApplicationLocales(locales)

                        // 3) Reiniciar la app
                        showLanguageDialog = false
                        pendingLanguage = null

                        activity?.let {
                            it.finishAffinity()
                            it.startActivity(Intent(it, MainActivity::class.java))
                        }
                    }
                ) {
                    Text(stringResource(R.string.dialog_restart_now))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLanguageDialog = false
                        pendingLanguage = null
                    }
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

}


// ======================= COMPONENTES =======================

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
    Divider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 24.dp)
    )
}

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
