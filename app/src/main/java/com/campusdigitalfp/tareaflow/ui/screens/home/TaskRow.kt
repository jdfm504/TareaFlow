package com.campusdigitalfp.tareaflow.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onLongClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val background = if (selected) cs.primary.copy(alpha = 0.15f) else cs.surface
    val borderColor = if (selected) cs.primary else cs.outline
    val titleColor = cs.onSurface
    val descColor = cs.onSurfaceVariant
    val iconColor = cs.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (task.done) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )

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

            IconButton(onClick = { /* toggle done */ }) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Marcar hecho",
                    tint = iconColor
                )
            }
        }
    }
}
