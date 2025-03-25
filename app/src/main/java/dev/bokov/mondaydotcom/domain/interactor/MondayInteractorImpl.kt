package dev.bokov.mondaydotcom.domain.interactor

import dev.bokov.mondaydotcom.domain.model.Board
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import dev.bokov.mondaydotcom.domain.model.Task
import dev.bokov.mondaydotcom.domain.repository.MondayRepository
import javax.inject.Inject

/**
 * Implementation of [MondayInteractor] that delegates calls to [MondayRepository]
 *
 * @property repository Repository used to perform data operations related to Monday.com
 */
internal class MondayInteractorImpl @Inject constructor(
    private val repository: MondayRepository
) : MondayInteractor {

    /**
     * @see MondayInteractor.getBoards
     */
    override suspend fun getBoards(): List<Board> {
        return repository.getBoardList()
    }

    /**
     * @see MondayInteractor.getTasksByBoardId
     */
    override suspend fun getTasksByBoardId(boardId: String): Map<String, List<Task>> {
        return repository
            .getBoardItems(boardId)
            .flatMap { boardWithItems ->
                val boardIdValue = boardWithItems.id
                val items = boardWithItems.items

                items.mapNotNull { item ->
                    val statusColumn =
                        item.columnValues.firstOrNull { col ->
                            col.id == STATUS && col.text != null
                        }

                    val statusValue = statusColumn?.text.orEmpty()
                    val columnId = statusColumn?.id

                    if (columnId != null) {
                        Task(
                            id = item.id,
                            name = item.name,
                            status = statusValue,
                            columnId = columnId,
                            boardId = boardIdValue,
                        )
                    } else {
                        null
                    }
                }
            }
            .groupBy { it.status.ifBlank { NO_STATUS } }
            .toList()
            .sortedWith(
                compareBy(
                    { (status, _) -> status.equals(DONE, ignoreCase = true) }, // Done is last
                    { (status, _) -> status }, // The rest is sorted alphabetically
                )
            )
            .toMap(LinkedHashMap()) // Keep the order
    }

    /**
     * @see MondayInteractor.changeStatus
     */
    override suspend fun changeStatus(
        boardId: String,
        itemId: String,
        columnId: String,
        statusText: String,
    ) {
        repository.changeTaskStatus(
            boardId = boardId,
            itemId = itemId,
            columnId = columnId,
            statusText = statusText,
        )
    }

    /**
     * @see MondayInteractor.getAvailableStatuses
     */
    override suspend fun getAvailableStatuses(boardId: String): List<BoardStatusOption> {
        return repository.getAvailableStatuses(boardId)
    }

    companion object {
        private const val DONE = "Done"
        private const val STATUS = "status"
        private const val NO_STATUS = "No Status"
    }
}
