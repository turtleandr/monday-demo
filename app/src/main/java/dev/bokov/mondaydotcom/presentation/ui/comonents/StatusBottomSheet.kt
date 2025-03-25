package dev.bokov.mondaydotcom.presentation.ui.comonents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatusBottomSheet(
    currentStatus: String,
    availableStatuses: List<BoardStatusOption>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Choose Status", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(availableStatuses) { status ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable { selectedStatus = status.label }
                                .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedStatus == status.label,
                            onClick = { selectedStatus = status.label },
                        )
                        Text(text = status.label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave(selectedStatus) },
                enabled = selectedStatus != currentStatus,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            ) {
                Text("Update Task")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
