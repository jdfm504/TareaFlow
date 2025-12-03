package com.campusdigitalfp.tareaflow.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel(),
    onGoToSettings: () -> Unit = {},
    onGoToAbout: () -> Unit = {}
) {
    //LaunchedEffect(Unit) { viewModel.seed() } // Lista de prueba local
    val tasks by viewModel.tasks.collectAsState() // Lista de firestore

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TareaFlow") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menú")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary) },
                            text = { Text("Agregar tarea") },
                            onClick = {
                                menuExpanded = false
                                viewModel.addTask("Nueva tarea", "Descripción")
                            }
                        )
                        if (viewModel.isActionMode) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.primary) },
                                text = { Text("Eliminar seleccionadas") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.deleteSelected()
                                }
                            )
                        }
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Filled.Settings, null, tint = MaterialTheme.colorScheme.primary) },
                            text = { Text("Configuración") },
                            onClick = {
                                menuExpanded = false
                                onGoToSettings()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary) },
                            text = { Text("Acerca de") },
                            onClick = {
                                menuExpanded = false
                                onGoToAbout()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addTask("Nueva tarea", "Desde FAB") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    selected = viewModel.selected.contains(task.id),
                    onClick = {
                        if (viewModel.isActionMode) viewModel.toggleSelection(task.id)
                        else {
                        }
                    },
                    onLongClick = { viewModel.toggleSelection(task.id) }
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
