package dev.bokov.mondaydotcom.domain.interactor

import dev.bokov.mondaydotcom.domain.model.Board
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import dev.bokov.mondaydotcom.domain.model.Task

/**
 * Interactor for interacting with Monday.com API-related domain logic
 */
internal interface MondayInteractor {

    /**
     * Returns a list of available boards for the current user
     */
    suspend fun getBoards(): List<Board>

    /**
     * Returns a map of tasks grouped by status for the specified board
     *
     * @param boardId ID of the board to fetch tasks from
     */
    suspend fun getTasksByBoardId(boardId: String): Map<String, List<Task>>

    /**
     * Changes the status of a specific task in a given board
     *
     * @param boardId ID of the board containing the task
     * @param itemId ID of the task (item) to update
     * @param columnId ID of the status column
     * @param statusText New status to set for the task
     */
    suspend fun changeStatus(
        boardId: String,
        itemId: String,
        columnId: String,
        statusText: String
    )

    /**
     * Returns the list of available statuses for a specific board
     *
     * @param boardId ID of the board to fetch status options from
     */
    suspend fun getAvailableStatuses(boardId: String): List<BoardStatusOption>
}
