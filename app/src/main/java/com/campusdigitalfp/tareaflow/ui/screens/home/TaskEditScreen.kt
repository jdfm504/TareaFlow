package com.campusdigitalfp.tareaflow.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.TaskViewModel
import com.campusdigitalfp.tareaflow.R
import com.campusdigitalfp.tareaflow.ui.theme.ApplyStatusBarTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    taskId: String? = null
) {
    val cs = MaterialTheme.colorScheme
    val existingTasks by viewModel.tasks.collectAsState(initial = emptyList())

    // Intentamos recuperar la tarea si estamos en modo edición
    val existingTask = existingTasks.find { it.id == taskId }
    val isEditing = taskId != null

    // Estados editables del título y la descripción
    var title by rememberSaveable(taskId) { mutableStateOf("") }
    var description by rememberSaveable(taskId) { mutableStateOf("") }

    // Flag para saber cuándo ya podemos mostrar los TextFields sin “saltos”
    var initialized by rememberSaveable(taskId) { mutableStateOf(false) }

    ApplyStatusBarTheme()

    // Este bloque sincroniza los datos cuando Firestore entrega la tarea
    LaunchedEffect(isEditing, existingTask?.id) {
        if (isEditing) {
            if (existingTask != null) {
                title = existingTask.title
                description = existingTask.description
                initialized = true
            }
        } else {
            // Nueva tarea
            initialized = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cs.background
    ) {
        if (!initialized) {
            //  Pantalla de carga mínima
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = cs.primary)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Título de pantalla
                Text(
                    text = if (taskId == null)
                        stringResource(R.string.new_task_title)
                    else
                        stringResource(R.string.edit_task_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = cs.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Campo Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(R.string.cancel_button),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                if (taskId == null) {
                                    // Nueva tarea
                                    viewModel.addTask(title, description)
                                } else {
                                    // Editar tarea
                                    existingTask?.let { safeTask ->
                                        viewModel.updateTask(
                                            safeTask.copy(
                                                title = title,
                                                description = description
                                            )
                                        )
                                    }
                                }
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringResource(R.string.save_button),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}