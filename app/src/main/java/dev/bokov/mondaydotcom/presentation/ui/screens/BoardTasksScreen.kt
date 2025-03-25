package dev.bokov.mondaydotcom.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.bokov.mondaydotcom.presentation.ui.components.TaskItem
import dev.bokov.mondaydotcom.presentation.viewmodel.BoardTasksViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
internal fun BoardTasksScreen(
    boardId: String,
    boardName: String,
    viewModel: BoardTasksViewModel = hiltViewModel(), // For simplicity, we use Hilt here
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableStatuses = uiState.availableStatuses
    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = uiState.isLoading,
            onRefresh = { viewModel.loadTasks(boardId) },
        )

    LaunchedEffect(boardId) {
        if (!uiState.isLoading) {
            viewModel.loadTasks(boardId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks on: $boardName") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().pullRefresh(pullRefreshState).padding(padding)) {
            when {
                uiState.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}")
                    }
                }

                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        uiState.itemsGroupedByStatus.forEach { (status, items) ->
                            item {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier =
                                        Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                                )
                            }

                            items(items, key = { it.id }, contentType = { "task" }) { task ->
                                TaskItem(
                                    task = task,
                                    availableStatuses = availableStatuses,
                                    onStatusSelected = { taskId, columnId, boardId, newStatus ->
                                        viewModel.onTaskCheckedChanged(
                                            taskId = taskId,
                                            columnId = columnId,
                                            boardId = boardId,
                                            newStatus = newStatus,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
