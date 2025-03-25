package dev.bokov.mondaydotcom.data.repository

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.MockServer
import com.apollographql.apollo3.mockserver.enqueue
import dev.bokov.mondaydotcom.domain.model.Board
import dev.bokov.mondaydotcom.domain.model.BoardItemColumn
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Tests for [MondayRepositoryImpl] */
@ApolloExperimental
@ExperimentalSerializationApi
class MondayRepositoryImplTest {

    private lateinit var mockServer: MockServer
    private lateinit var apolloClient: ApolloClient
    private lateinit var repository: MondayRepositoryImpl

    @Before
    fun setUp() {
        mockServer = MockServer()
    }

    @After
    fun tearDown() {
        runTest { mockServer.stop() }
        apolloClient.close()
    }

    private suspend fun initializeClientAndRepository() {
        apolloClient = ApolloClient.Builder().serverUrl(mockServer.url()).build()
        repository = MondayRepositoryImpl(apolloClient)
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun `getBoardList returns list of boards when response is successful`() = runTest {
        // GIVEN: Initialize client and repository in suspend context
        initializeClientAndRepository()

        val responseJson =
            buildJsonObject {
                    put(
                        "data",
                        buildJsonObject {
                            put(
                                "boards",
                                Json.parseToJsonElement(
                                    """
                    [
                        {"id": "1", "name": "Board 1"},
                        {"id": "2", "name": "Board 2"}
                    ]
                """
                                ),
                            )
                        },
                    )
                }
                .toString()

        mockServer.enqueue(responseJson)

        // WHEN: getBoardList is called
        val result = repository.getBoardList()

        // THEN: The result contains the mapped boards
        assertEquals(2, result.size)
        assertEquals(Board(id = "1", name = "Board 1"), result[0])
        assertEquals(Board(id = "2", name = "Board 2"), result[1])
    }

    @Test
    fun `getBoardList returns empty list when response has no boards`() = runTest {
        // GIVEN: Initialize client and repository in suspend context
        initializeClientAndRepository()

        val responseJson =
            buildJsonObject {
                    put("data", buildJsonObject { put("boards", Json.parseToJsonElement("[]")) })
                }
                .toString()

        mockServer.enqueue(responseJson)

        // WHEN: getBoardList is called
        val result = repository.getBoardList()

        // THEN: The result is an empty list
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getBoardList returns empty list when response data is null`() = runTest {
        // GIVEN: Initialize client and repository in suspend context
        initializeClientAndRepository()

        val responseJson = buildJsonObject { put("data", null) }.toString()

        mockServer.enqueue(responseJson)

        // WHEN: getBoardList is called
        val result = repository.getBoardList()

        // THEN: The result is an empty list
        assertTrue(result.isEmpty())
    }

    // Tests for getBoardItems()
    @Test
    fun `getBoardItems returns list of boards with items when response is successful`() = runTest {
        // GIVEN: Initialize client and repository
        initializeClientAndRepository()

        val responseJson =
            buildJsonObject {
                    put(
                        "data",
                        buildJsonObject {
                            put(
                                "boards",
                                Json.parseToJsonElement(
                                    """
                    [
                        {
                            "id": "board1",
                            "name": "Test Board",
                            "items_page": {
                                "items": [
                                    {
                                        "id": "item1",
                                        "name": "Task 1",
                                        "column_values": [
                                            {"id": "status", "text": "Done"}
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                """
                                ),
                            )
                        },
                    )
                }
                .toString()

        mockServer.enqueue(responseJson)

        // WHEN: getBoardItems is called with a board ID
        val result = repository.getBoardItems("board1")

        // THEN: The result contains the mapped boards with items
        assertEquals(1, result.size)
        val board = result[0]
        assertEquals("board1", board.id)
        assertEquals("Test Board", board.name)
        assertEquals(1, board.items.size)
        val item = board.items[0]
        assertEquals("item1", item.id)
        assertEquals("Task 1", item.name)
        assertEquals(1, item.columnValues.size)
        assertEquals(BoardItemColumn(id = "status", text = "Done"), item.columnValues[0])
    }

    @Test
    fun `getBoardItems returns empty list when response has no boards`() = runTest {
        // GIVEN: Initialize client and repository
        initializeClientAndRepository()

        val responseJson =
            buildJsonObject {
                    put("data", buildJsonObject { put("boards", Json.parseToJsonElement("[]")) })
                }
                .toString()

        mockServer.enqueue(responseJson)

        // WHEN: getBoardItems is called
        val result = repository.getBoardItems("board1")

        // THEN: The result is an empty list
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getBoardItems throws exception when response has errors`() = runTest {
        // GIVEN: Initialize client and repository
        initializeClientAndRepository()

        val responseJson =
            buildJsonObject {
                    put("errors", Json.parseToJsonElement("""[{"message": "Invalid board ID"}]"""))
                    put("data", null)
                }
                .toString()

        mockServer.enqueue(responseJson)

        // WHEN & THEN: getBoardItems throws an exception
        val exception = assertFailsWith<Exception> { repository.getBoardItems("board1") }
        assertEquals("GraphQL error: Invalid board ID", exception.message)
    }

    // Tests for getAvailableStatuses()
    @Test
    fun `getAvailableStatuses returns list of status options when response has status column`() =
        runTest {
            initializeClientAndRepository()
            val responseJson =
                buildJsonObject {
                        put(
                            "data",
                            buildJsonObject {
                                put(
                                    "boards",
                                    Json.parseToJsonElement(
                                        """
                    [
                        {
                            "columns": [
                                {
                                    "id": "col1",
                                    "title": "Status",
                                    "type": "status",
                                    "settings_str": "{\"labels\": {\"1\": \"Done\", \"2\": \"In Progress\"}}"
                                }
                            ]
                        }
                    ]
                """
                                    ),
                                )
                            },
                        )
                    }
                    .toString()

            mockServer.enqueue(responseJson)
            val result = repository.getAvailableStatuses("board1")

            assertEquals(2, result.size)
            assertEquals(BoardStatusOption(label = "Done"), result[0])
            assertEquals(BoardStatusOption(label = "In Progress"), result[1])
        }

    @Test
    fun `getAvailableStatuses returns empty list when no status column exists`() = runTest {
        initializeClientAndRepository()
        val responseJson =
            buildJsonObject {
                    put(
                        "data",
                        buildJsonObject {
                            put(
                                "boards",
                                Json.parseToJsonElement(
                                    """
                    [
                        {
                            "columns": [
                                {
                                    "id": "col1",
                                    "title": "Text",
                                    "type": "text",
                                    "settings_str": "{}"
                                }
                            ]
                        }
                    ]
                """
                                ),
                            )
                        },
                    )
                }
                .toString()

        mockServer.enqueue(responseJson)
        val result = repository.getAvailableStatuses("board1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailableStatuses returns empty list when no labels in settings`() = runTest {
        initializeClientAndRepository()
        val responseJson =
            buildJsonObject {
                    put(
                        "data",
                        buildJsonObject {
                            put(
                                "boards",
                                Json.parseToJsonElement(
                                    """
                    [
                        {
                            "columns": [
                                {
                                    "id": "col1",
                                    "title": "Status",
                                    "type": "status",
                                    "settings_str": "{}"
                                }
                            ]
                        }
                    ]
                """
                                ),
                            )
                        },
                    )
                }
                .toString()

        mockServer.enqueue(responseJson)
        val result = repository.getAvailableStatuses("board1")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailableStatuses filters out blank labels`() = runTest {
        initializeClientAndRepository()
        val responseJson =
            buildJsonObject {
                    put(
                        "data",
                        buildJsonObject {
                            put(
                                "boards",
                                Json.parseToJsonElement(
                                    """
                    [
                        {
                            "columns": [
                                {
                                    "id": "col1",
                                    "title": "Status",
                                    "type": "status",
                                    "settings_str": "{\"labels\": {\"1\": \"Done\", \"2\": \"\", \"3\": \"In Progress\"}}"
                                }
                            ]
                        }
                    ]
                """
                                ),
                            )
                        },
                    )
                }
                .toString()

        mockServer.enqueue(responseJson)
        val result = repository.getAvailableStatuses("board1")

        assertEquals(2, result.size)
        assertEquals(BoardStatusOption(label = "Done"), result[0])
        assertEquals(BoardStatusOption(label = "In Progress"), result[1])
    }
}
