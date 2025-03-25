package dev.bokov.mondaydotcom.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bokov.mondaydotcom.di.IoDispatcher
import dev.bokov.mondaydotcom.domain.interactor.MondayInteractor
import dev.bokov.mondaydotcom.presentation.model.BoardWithStatusSummaryUiModel
import dev.bokov.mondaydotcom.presentation.state.BoardsUiState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing the UI state of tasks grouped by status on a specific board
 *
 * @property interactor Interactor for Monday.com operations such as fetching tasks and statuses
 * @property ioDispatcher Coroutine dispatcher used for IO-bound operations
 */
@HiltViewModel
internal class BoardListViewModel
@Inject
constructor(
    private val interactor: MondayInteractor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardsUiState(isLoading = true))
    val uiState: StateFlow<BoardsUiState> = _uiState

    init {
        fetchBoards()
    }

    fun fetchBoards() {
        viewModelScope.launch {
            _uiState.value = BoardsUiState(isLoading = true, boards = _uiState.value.boards)

            runCatching {
                withContext(ioDispatcher) {
                    val boards = interactor.getBoards()
                    boards.map { board ->
                        val groupedTasks = interactor.getTasksByBoardId(board.id)
                        val statusCounts = groupedTasks.mapValues { it.value.size }

                        BoardWithStatusSummaryUiModel(
                            id = board.id,
                            name = board.name,
                            statusCounts = statusCounts,
                        )
                    }
                }
            }
                .onSuccess { boards -> _uiState.value = BoardsUiState(boards = boards) }
                .onFailure { e ->
                    _uiState.value = BoardsUiState(error = e.localizedMessage ?: "Error", boards = _uiState.value.boards)
                }
        }
    }
}