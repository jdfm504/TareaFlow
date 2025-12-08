package com.campusdigitalfp.tareaflow.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusdigitalfp.tareaflow.data.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRow(
    task: Task,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleDone: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isLightTheme = !isSystemInDarkTheme()

    // Fondo animado al seleccionar
    val background by animateColorAsState(
        targetValue = when {
            selected && !isLightTheme -> Color(0xFF2C2C40) // fondo oscuro elegante
            selected && isLightTheme -> Color(0xFFE9E7FF)  // lila pastel claro
            else -> cs.surface
        },
        label = "background"
    )

    // Escala y elevación animadas al seleccionar
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        label = "scale"
    )
    val elevation = if (selected) 8.dp else 2.dp

    val titleColor = cs.onSurface
    val descColor = cs.onSurfaceVariant
    val iconColor = cs.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale) // efecto suave de realce
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = titleColor,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = descColor,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Botón de marcar hecho
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier.size(42.dp)
            ) {
                Crossfade(targetState = task.done, label = "doneTransition") { isDone ->
                    if (isDone) {
                        // Círculo verde con tick blanco grande
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF31B466), shape = CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Marcar como pendiente",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        // Círculo gris vacío
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = "Marcar como completada",
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
