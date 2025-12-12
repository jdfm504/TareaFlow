package com.campusdigitalfp.tareaflow.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.campusdigitalfp.tareaflow.R
import kotlinx.coroutines.launch
import com.campusdigitalfp.tareaflow.ui.theme.GreenDark
import com.campusdigitalfp.tareaflow.data.model.Task
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import kotlin.text.contains

@Composable
fun SectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f, // gira al desplegar
        label = "arrowRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer { rotationZ = rotation } // animación
        )

        Spacer(Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TaskGroup(
    title: String,
    tasks: List<Task>,
    expanded: Boolean,
    onToggle: () -> Unit,
    isActionMode: Boolean,
    isSelected: (String) -> Boolean,
    onRowClick: (Task) -> Unit,
    onRowLongClick: (Task) -> Unit,
    onToggleDone: (Task) -> Unit
) {
    // Cabecera con flecha giratoria
    SectionHeader(
        title = "$title (${tasks.size})",
        expanded = expanded,
        onToggle = onToggle
    )

    // Animación global del grupo (expand/contract)
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column {
            tasks.forEach { task ->
                // Estado de aparición individual por tarea (estable por id)
                var itemVisible by remember(task.id) { mutableStateOf(false) }
                LaunchedEffect(task.id) { itemVisible = true }

                androidx.compose.runtime.key(task.id) {
                    // Animación de entrada por item
                    AnimatedVisibility(
                        visible = itemVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut()
                    ) {
                        val offsetY by animateFloatAsState(
                            targetValue = if (itemVisible) 0f else 30f,
                            label = "ItemOffset"
                        )
                        Box(Modifier.offset(y = offsetY.dp)) {
                            TaskRow(
                                task = task,
                                selected = isSelected(task.id),
                                onClick = {
                                    if (isActionMode) onRowLongClick(task) else onRowClick(task)
                                },
                                onLongClick = { onRowLongClick(task) },
                                onToggleDone = { onToggleDone(task) }
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    onGoToSettings: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    //LaunchedEffect(Unit) { viewModel.seed() } // Lista de prueba local
    val tasks by viewModel.tasks.collectAsState() // Lista de firestore
    val context = LocalContext.current

    var menuExpanded by remember { mutableStateOf(false) }

    // colores animados para TopBar y FAB
    val topBarColor by animateColorAsState(
        targetValue = if (viewModel.isActionMode)
            GreenDark
        else
            MaterialTheme.colorScheme.primary,
        label = "TopBarColor"
    )

    val fabColor by animateColorAsState(
        targetValue = if (viewModel.isActionMode)
            MaterialTheme.colorScheme.errorContainer
        else
            MaterialTheme.colorScheme.primary,
        label = "FabColor"
    )

    // snackbar + scope para lanzarlo
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Variables para agrupar completadas y pendientes
    var showPending by remember { mutableStateOf(true) }
    var showCompleted by remember { mutableStateOf(false) }

    val isAnonymous = FirebaseAuth.getInstance().currentUser?.isAnonymous == true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    containerColor =  topBarColor,
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
                    AnimatedVisibility(
                        visible = viewModel.isActionMode,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 })
                    ) {
                        DeleteButton {
                            viewModel.deleteSelected()
                            coroutineScope.launch {
                                val msg = context.getString(R.string.tasks_deleted_success)
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    }

                    //  Menú normal
                    if (!viewModel.isActionMode) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.menu_cd)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary)
                                },
                                text = { Text(stringResource(R.string.menu_add_task)) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("task/new")
                                }
                            )

                            if (isAnonymous) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.PersonAdd,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    text = { Text(stringResource(R.string.menu_upgrade_account)) },
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate("register")
                                    }
                                )
                            }

                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Filled.Settings, null, tint = MaterialTheme.colorScheme.tertiary)
                                },
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    menuExpanded = false
                                    onGoToSettings()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary)
                                },
                                text = { Text(stringResource(R.string.menu_about)) },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("about")
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
                                    // paramos listener y limpiamos estado
                                    viewModel.stopListeningToTasks()
                                    authViewModel.logout()
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
                    containerColor = fabColor
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.fab_add_cd))
                }
            }
        }
    ) { padding ->

        // Agrupar tareas según estado
        val pendingTasks = tasks.filter { !it.done }
        val completedTasks = tasks.filter { it.done }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // PENDIENTES (un solo item que contiene cabecera + grupo)
            item {
                TaskGroup(
                    title = stringResource(R.string.tasks_pending),
                    tasks = pendingTasks,
                    expanded = viewModel.showPending,
                    onToggle = { viewModel.togglePending() },
                    isActionMode = viewModel.isActionMode,
                    isSelected = { id -> viewModel.selected.contains(id) },
                    onRowClick = { task -> navController.navigate("task/${task.id}") },
                    onRowLongClick = { task -> viewModel.toggleSelection(task.id) },
                    onToggleDone = { task -> viewModel.toggleDone(task.id) }
                )
            }

            // Separador visual entre grupos
            item { Spacer(Modifier.height(12.dp)) }

            // COMPLETADAS (mismo patrón)
            item {
               TaskGroup(
                   title = stringResource(R.string.tasks_completed),
                    tasks = completedTasks,
                   expanded = viewModel.showCompleted,
                   onToggle = { viewModel.toggleCompleted() },
                    isActionMode = viewModel.isActionMode,
                    isSelected = { id -> viewModel.selected.contains(id) },
                    onRowClick = { task -> navController.navigate("task/${task.id}") },
                    onRowLongClick = { task -> viewModel.toggleSelection(task.id) },
                    onToggleDone = { task -> viewModel.toggleDone(task.id) }
                )
            }
        }
    }
}
