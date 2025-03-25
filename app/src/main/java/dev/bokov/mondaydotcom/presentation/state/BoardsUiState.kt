package dev.bokov.mondaydotcom.presentation.state

import dev.bokov.mondaydotcom.presentation.model.BoardWithStatusSummaryUiModel

/**
 * Represents the UI state for the screen displaying a list of boards
 *
 * @property isLoading Indicates whether the data is currently being loaded
 * @property boards List of boards with task status summaries to be shown on screen
 * @property error Optional error message to display in case of a loading failure
 *
 * See also: [BoardWithStatusSummaryUiModel]
 */
internal data class BoardsUiState(
    val isLoading: Boolean = false,
    val boards: List<BoardWithStatusSummaryUiModel> = emptyList(),
    val error: String? = null
)
