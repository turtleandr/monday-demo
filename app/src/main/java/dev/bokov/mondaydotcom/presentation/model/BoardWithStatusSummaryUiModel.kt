package dev.bokov.mondaydotcom.presentation.model

/**
 * UI model that represents a board along with a summary of task counts per status
 *
 * @property id Unique identifier of the board
 * @property name Display name of the board
 * @property statusCounts Map of status label to number of tasks in that status
 *
 * See also: [BoardsUiState] where this model is used to render board list
 */
internal data class BoardWithStatusSummaryUiModel(
    val id: String,
    val name: String,
    val statusCounts: Map<String, Int>
)