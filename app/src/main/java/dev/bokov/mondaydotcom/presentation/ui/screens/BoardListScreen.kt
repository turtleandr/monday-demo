@file:OptIn(ExperimentalMaterial3Api::class)

package dev.bokov.mondaydotcom.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.bokov.mondaydotcom.presentation.ui.comonents.BoardCard
import dev.bokov.mondaydotcom.presentation.ui.comonents.ErrorContent
import dev.bokov.mondaydotcom.presentation.viewmodel.BoardListViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun BoardListScreen(
    viewModel: BoardListViewModel = hiltViewModel(),
    onBoardClick: (String, String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState =
        rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = viewModel::fetchBoards)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Boards on Monday.com") }) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState).padding(padding)) {
            when {
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error.toString(),
                        onRetry = viewModel::fetchBoards,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(
                            items = uiState.boards,
                            key = { it.id },
                            contentType = { "board_item" },
                        ) { board ->
                            BoardCard(boardName = board.name, statusCounts = board.statusCounts) {
                                onBoardClick(board.id, board.name)
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
