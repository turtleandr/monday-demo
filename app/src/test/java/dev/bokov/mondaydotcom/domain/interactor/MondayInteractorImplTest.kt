package dev.bokov.mondaydotcom.domain.interactor

import dev.bokov.mondaydotcom.domain.model.Board
import dev.bokov.mondaydotcom.domain.model.BoardItem
import dev.bokov.mondaydotcom.domain.model.BoardItemColumn
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import dev.bokov.mondaydotcom.domain.model.BoardWithTasks
import dev.bokov.mondaydotcom.domain.model.Task
import dev.bokov.mondaydotcom.domain.repository.MondayRepository
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Tests for [MondayInteractorImpl] */
class MondayInteractorImplTest {

    private lateinit var repository: MondayRepository
    private lateinit var interactor: MondayInteractorImpl

    @Before
    fun setUp() {
        repository = mockk(relaxed = true) // Relaxed mock to avoid unmocked call issues
        interactor = MondayInteractorImpl(repository)
    }

    @After
    fun tearDown() {
        clearMocks(repository)
    }

    // Tests for getBoards()
    @Test
    fun `getBoards returns list of boards when repository provides boards`() = runTest {
        // GIVEN
        val boards = listOf(Board(id = "1", name = "Board 1"), Board(id = "2", name = "Board 2"))
        coEvery { repository.getBoardList() } returns boards

        // WHEN
        val result = interactor.getBoards()

        // THEN
        assertEquals(boards, result)
        coVerify(exactly = 1) { repository.getBoardList() }
    }

    @Test
    fun `getBoards returns empty list when repository returns empty`() = runTest {
        // GIVEN
        coEvery { repository.getBoardList() } returns emptyList()

        // WHEN
        val result = interactor.getBoards()

        // THEN
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { repository.getBoardList() }
    }

    // Tests for getTasksByBoardId()
    @Test
    fun `getTasksByBoardId returns tasks grouped by status with Done last`() = runTest {
        // GIVEN
        val boardId = "board1"
        val boardItems =
            listOf(
                BoardWithTasks(
                    id = boardId,
                    name = "Test Board",
                    items =
                        listOf(
                            BoardItem(
                                id = "item1",
                                name = "Task 1",
                                columnValues =
                                    listOf(BoardItemColumn(id = "status", text = "In Progress")),
                            ),
                            BoardItem(
                                id = "item2",
                                name = "Task 2",
                                columnValues = listOf(BoardItemColumn(id = "status", text = "Done")),
                            ),
                            BoardItem(
                                id = "item3",
                                name = "Task 3",
                                columnValues = listOf(BoardItemColumn(id = "status", text = "Todo")),
                            ),
                        ),
                )
            )
        coEvery { repository.getBoardItems(boardId) } returns boardItems

        // WHEN
        val result = interactor.getTasksByBoardId(boardId)

        // THEN
        assertEquals(3, result.size)
        val expectedOrder = listOf("In Progress", "Todo", "Done")
        assertEquals(expectedOrder, result.keys.toList())
        assertEquals(
            listOf(
                Task(
                    id = "item1",
                    name = "Task 1",
                    status = "In Progress",
                    columnId = "status",
                    boardId = boardId,
                )
            ),
            result["In Progress"],
        )
        assertEquals(
            listOf(
                Task(
                    id = "item3",
                    name = "Task 3",
                    status = "Todo",
                    columnId = "status",
                    boardId = boardId,
                )
            ),
            result["Todo"],
        )
        assertEquals(
            listOf(
                Task(
                    id = "item2",
                    name = "Task 2",
                    status = "Done",
                    columnId = "status",
                    boardId = boardId,
                )
            ),
            result["Done"],
        )
        coVerify(exactly = 1) { repository.getBoardItems(boardId) }
    }

    @Test
    fun `getTasksByBoardId returns empty map when no status column exists`() = runTest {
        // GIVEN
        val boardId = "board1"
        val boardItems =
            listOf(
                BoardWithTasks(
                    id = boardId,
                    name = "Test Board",
                    items =
                        listOf(
                            BoardItem(
                                id = "item1",
                                name = "Task 1",
                                columnValues = listOf(BoardItemColumn(id = "other", text = "Value")),
                            )
                        ),
                )
            )
        coEvery { repository.getBoardItems(boardId) } returns boardItems

        // WHEN
        val result = interactor.getTasksByBoardId(boardId)

        // THEN
        assertTrue(result.isEmpty()) // Expect empty map since no "status" column
        coVerify(exactly = 1) { repository.getBoardItems(boardId) }
    }

    @Test
    fun `getTasksByBoardId returns empty map when no boards returned`() = runTest {
        // GIVEN
        val boardId = "board1"
        coEvery { repository.getBoardItems(boardId) } returns emptyList()

        // WHEN
        val result = interactor.getTasksByBoardId(boardId)

        // THEN
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { repository.getBoardItems(boardId) }
    }

    // Tests for changeStatus()
    @Test
    fun `changeStatus calls repository with correct parameters`() = runTest {
        // GIVEN
        val boardId = "board1"
        val itemId = "item1"
        val columnId = "status"
        val statusText = "Done"
        coEvery { repository.changeTaskStatus(boardId, itemId, columnId, statusText) } returns Unit

        // WHEN
        interactor.changeStatus(boardId, itemId, columnId, statusText)

        // THEN
        coVerify(exactly = 1) { repository.changeTaskStatus(boardId, itemId, columnId, statusText) }
    }

    // Tests for getAvailableStatuses()
    @Test
    fun `getAvailableStatuses returns status options when repository provides them`() = runTest {
        // GIVEN
        val boardId = "board1"
        val statuses =
            listOf(BoardStatusOption(label = "Done"), BoardStatusOption(label = "In Progress"))
        coEvery { repository.getAvailableStatuses(boardId) } returns statuses

        // WHEN
        val result = interactor.getAvailableStatuses(boardId)

        // THEN
        assertEquals(statuses, result)
        coVerify(exactly = 1) { repository.getAvailableStatuses(boardId) }
    }

    @Test
    fun `getAvailableStatuses returns empty list when repository returns empty`() = runTest {
        // GIVEN
        val boardId = "board1"
        coEvery { repository.getAvailableStatuses(boardId) } returns emptyList()

        // WHEN
        val result = interactor.getAvailableStatuses(boardId)

        // THEN
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { repository.getAvailableStatuses(boardId) }
    }
}
