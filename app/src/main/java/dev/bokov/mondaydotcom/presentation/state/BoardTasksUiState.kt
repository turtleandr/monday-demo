package dev.bokov.mondaydotcom.presentation.state

import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import dev.bokov.mondaydotcom.domain.model.Task

/**
 * Represents the UI state for a screen displaying tasks of a specific board
 *
 * @property isLoading Indicates whether tasks and statuses are being loaded
 * @property itemsGroupedByStatus Tasks grouped by their status labels
 * @property availableStatuses List of all available status options for the board
 * @property error Optional error message to display in case of a failure
 *
 * See also: [Task], [BoardStatusOption]
 */
internal data class BoardTasksUiState(
    val isLoading: Boolean = false,
    val itemsGroupedByStatus: Map<String, List<Task>> = emptyMap(),
    val availableStatuses: List<BoardStatusOption> = emptyList(),
    val error: String? = null
)