package com.campusdigitalfp.tareaflow.ui.screens.settings

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.campusdigitalfp.tareaflow.ui.theme.GreenPrimary
import com.campusdigitalfp.tareaflow.R
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    prefsViewModel: PreferencesViewModel = viewModel()
) {
    val prefs by prefsViewModel.prefs.collectAsState()

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    // Estados de contraseña (NO van a Firestore)
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {

        // ------------------ TITULO ------------------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = GreenPrimary
                )
            }

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
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

        // ======================= IDIOMA =======================
        SectionHeader(
            title = stringResource(R.string.settings_section_language),
            description = stringResource(R.string.settings_section_language_desc)
        )

        LanguageSegmentedControl(
            selected = prefs.language,
            onSelect = { prefsViewModel.setLanguage(it) }
        )

        SoftDivider()

        // ======================= POMODORO =======================
        SectionHeader(
            title = stringResource(R.string.settings_section_pomodoro),
            description = stringResource(R.string.settings_section_pomodoro_desc)
        )

        OutlinedTextField(
            value = prefs.pomodoroMinutes.toString(),
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() } && newValue.isNotEmpty()) {
                    prefsViewModel.setPomodoro(newValue.toInt())
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
                .border(1.dp, Color(0xFFDADADA), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text(stringResource(R.string.settings_password_current)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text(stringResource(R.string.settings_password_new)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                label = { Text(stringResource(R.string.settings_password_repeat)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(Modifier.height(40.dp))

        // ======================= BOTÓN BORRAR CUENTA =======================
        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(stringResource(R.string.settings_delete_account), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(60.dp))

    }

    // ------------------ DIALOGO ------------------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message))  },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_yes_delete), color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF666666)),
            modifier = Modifier.padding(bottom = 10.dp)
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
        color = Color(0xFFE5E5E5),
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
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = Color(0xFFC8C8C8)
    )

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = switchColors
    )
}
@Composable
fun LanguageSegmentedControl(
    selected: String,
    onSelect: (String) -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = Color.White

    Row(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, selectedColor, RoundedCornerShape(16.dp))
            .padding(4.dp)
            .height(40.dp)
    ) {

        // Español
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected == "es") selectedColor else unselectedColor)
                .clickable { onSelect("es") }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.settings_lang_spanish),
                color = if (selected == "es") Color.White else Color.Black,
                fontWeight = FontWeight.Medium
            )
        }

        // Inglés
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected == "en") selectedColor else unselectedColor)
                .clickable { onSelect("en") }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.settings_lang_english),
                color = if (selected == "en") Color.White else Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
