package dev.bokov.mondaydotcom.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import dev.bokov.mondaydotcom.domain.model.Task
import dev.bokov.mondaydotcom.presentation.ui.comonents.StatusBottomSheet

@Composable
internal fun TaskItem(
    task: Task,
    availableStatuses: List<BoardStatusOption>,
    onStatusSelected: (taskId: String, columnId: String, boardId: String, newStatus: String) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    val isDone = task.status.equals("Done", ignoreCase = true)

    val containerColor by
        animateColorAsState(
            targetValue = if (isDone) Color(0xFFEAF7EA) else MaterialTheme.colorScheme.surface,
            animationSpec = tween(300),
        )

    val stripColor =
        if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Colored status line
            Box(
                modifier =
                    Modifier.width(4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(stripColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (task.status.isNotBlank()) {
                    Text(
                        text = task.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            IconButton(onClick = { showBottomSheet = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Status")
            }
        }
    }

    if (showBottomSheet) {
        StatusBottomSheet(
            currentStatus = task.status,
            availableStatuses = availableStatuses,
            onDismiss = { showBottomSheet = false },
            onSave = { selectedStatus ->
                showBottomSheet = false
                onStatusSelected(task.id, task.columnId, task.boardId, selectedStatus)
            },
        )
    }
}
