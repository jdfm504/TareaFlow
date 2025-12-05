package com.campusdigitalfp.tareaflow.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.campusdigitalfp.tareaflow.R

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
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (viewModel.isActionMode) {
                        Text(
                            text = stringResource(R.string.selection_mode_title, viewModel.selected.size),
                            color = Color.White
                        )
                    } else {
                        Text(stringResource(R.string.home_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (viewModel.isActionMode) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel_selection_cd))
                        }
                    }
                },
                actions = {
                    if (viewModel.isActionMode) {
                        // Botón de borrar visible solo en modo selección
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.menu_delete_selected),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        //  Menú normal
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.menu_cd)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Add,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                text = { Text(stringResource(R.string.menu_add_task)) },
                                onClick = {
                                    menuExpanded = false
                                    //viewModel.addTask("Nueva tarea", "Descripción")
                                    navController.navigate("task/new")
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Settings,
                                        null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                },
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    menuExpanded = false
                                    onGoToSettings()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Info,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                text = { Text(stringResource(R.string.menu_about)) },
                                onClick = {
                                    menuExpanded = false
                                    onGoToAbout()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ExitToApp,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                text = { Text(stringResource(R.string.menu_logout)) },
                                onClick = {
                                    menuExpanded = false
                                    auth.signOut()
                                    Toast.makeText(
                                        context,
                                        R.string.logout_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            )

        },
        floatingActionButton = {
            if (!viewModel.isActionMode) {
                FloatingActionButton(
                    onClick = { navController.navigate("task/new") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.fab_add_cd))
                }
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
                            navController.navigate("task/${task.id}")
                        }
                    },
                    onLongClick = { viewModel.toggleSelection(task.id) }
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
