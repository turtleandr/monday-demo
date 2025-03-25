package dev.bokov.mondaydotcom.domain.repository

import dev.bokov.mondaydotcom.domain.model.Board
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import dev.bokov.mondaydotcom.domain.model.BoardWithTasks

/**
 * Repository interface for accessing Monday.com-related data
 */
internal interface MondayRepository {

    /**
     * Returns a list of available boards for the user
     */
    suspend fun getBoardList(): List<Board>

    /**
     * Returns a list of board data with associated tasks for the given board ID
     *
     * @param boardId ID of the board to fetch tasks from
     */
    suspend fun getBoardItems(boardId: String): List<BoardWithTasks>

    /**
     * Changes the status of a task using board ID, item ID and column ID
     *
     * @param boardId ID of the board where the task belongs
     * @param itemId ID of the task to update
     * @param columnId ID of the column representing status
     * @param statusText New status label to apply
     */
    suspend fun changeTaskStatus(
        boardId: String,
        itemId: String,
        columnId: String,
        statusText: String
    )

    /**
     * Returns a list of available status options (labels) for the given board
     *
     * @param boardId ID of the board to fetch available statuses from
     */
    suspend fun getAvailableStatuses(boardId: String): List<BoardStatusOption>
}
