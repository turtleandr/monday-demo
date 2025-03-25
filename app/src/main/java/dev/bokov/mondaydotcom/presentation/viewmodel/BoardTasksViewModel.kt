package dev.bokov.mondaydotcom.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bokov.mondaydotcom.di.IoDispatcher
import dev.bokov.mondaydotcom.domain.interactor.MondayInteractor
import dev.bokov.mondaydotcom.presentation.state.BoardTasksUiState
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing the UI state of tasks grouped by status on a specific board
 *
 * @property interactor Interactor for Monday.com operations such as fetching tasks and statuses
 * @property ioDispatcher Coroutine dispatcher used for IO-operations
 */
@HiltViewModel
internal class BoardTasksViewModel
@Inject
constructor(
    private val interactor: MondayInteractor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardTasksUiState())
    val uiState: StateFlow<BoardTasksUiState> = _uiState

    /**
     * Loads the tasks and available statuses for the given board
     *
     * @param boardId ID of the board whose tasks should be loaded
     */
    fun loadTasks(boardId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = runCatching {
                withContext(ioDispatcher) {
                    val statusesDeferred = async { interactor.getAvailableStatuses(boardId) }
                    val tasksDeferred = async { interactor.getTasksByBoardId(boardId) }
                    statusesDeferred.await() to tasksDeferred.await()
                }
            }

            result.fold(
                onSuccess = { (statuses, groupedTasks) ->
                    _uiState.update {
                        it.copy(
                            itemsGroupedByStatus = groupedTasks,
                            availableStatuses = statuses,
                            isLoading = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = parseErrorMessage(e)) }
                },
            )
        }
    }

    /**
     * Updates the status of a specific task and reloads the board's tasks on success
     *
     * @param boardId ID of the board containing the task
     * @param taskId ID of the task to update
     * @param columnId ID of the status column
     * @param newStatus New status to set for the task
     */
    fun onTaskCheckedChanged(boardId: String, taskId: String, columnId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }

            runCatching {
                    withContext(ioDispatcher) {
                        interactor.changeStatus(
                            boardId = boardId,
                            itemId = taskId,
                            columnId = columnId,
                            statusText = newStatus,
                        )
                    }
                }
                .fold(
                    onSuccess = { loadTasks(boardId) },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(error = "Status update failed: ${parseErrorMessage(e)}")
                        }
                    },
                )
        }
    }

    /**
     * Parses a Throwable into a user-friendly error message
     *
     * @param e The throwable to parse
     * @return A user-readable error message
     */
    private fun parseErrorMessage(e: Throwable): String =
        when (e) {
            is IOException -> "Connection error"
            else -> e.localizedMessage ?: "Unknown error"
        }
}
