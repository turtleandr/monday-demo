package dev.bokov.mondaydotcom.data.repository

import com.apollographql.apollo3.ApolloClient
import dev.bokov.mondaydotcom.data.mapper.toDomain
import dev.bokov.mondaydotcom.domain.model.Board
import dev.bokov.mondaydotcom.domain.model.BoardStatusOption
import dev.bokov.mondaydotcom.domain.model.BoardWithTasks
import dev.bokov.mondaydotcom.domain.repository.MondayRepository
import dev.bokov.mondaydotcom.graphql.ChangeTaskStatusMutation
import dev.bokov.mondaydotcom.graphql.GetBoardColumnsQuery
import dev.bokov.mondaydotcom.graphql.GetBoardItemsQuery
import dev.bokov.mondaydotcom.graphql.GetBoardsQuery
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * Implementation of [MondayRepository] that provides access to Monday.com data using Apollo GraphQL
 * client
 *
 * @property apolloClient The Apollo GraphQL client used for communicating with the Monday.com API
 */
internal class MondayRepositoryImpl @Inject constructor(private val apolloClient: ApolloClient) :
    MondayRepository {

    /** @see MondayRepository.getBoardList */
    override suspend fun getBoardList(): List<Board> {
        val response = apolloClient.query(GetBoardsQuery()).execute()
        val boards = response.data?.boards.orEmpty()
        return boards.mapNotNull { gqlBoard -> gqlBoard?.toDomain() }
    }

    /** @see MondayRepository.getBoardItems */
    override suspend fun getBoardItems(boardId: String): List<BoardWithTasks> {
        val query = GetBoardItemsQuery(boardIds = listOf(boardId))
        val response = apolloClient.query(query).execute()

        if (response.hasErrors()) {
            throw Exception("GraphQL error: ${response.errors?.firstOrNull()?.message}")
        }

        val boards = response.data?.boards?.filterNotNull() ?: return emptyList()
        return boards.map { it.toDomain() }
    }

    /** @see MondayRepository.changeTaskStatus */
    override suspend fun changeTaskStatus(
        boardId: String,
        itemId: String,
        columnId: String,
        statusText: String,
    ) {
        val mutation =
            ChangeTaskStatusMutation(
                boardId = boardId,
                itemId = itemId,
                columnId = columnId,
                value = """{ "label": "$statusText" }""",
            )

        val response = apolloClient.mutation(mutation).execute()

        if (response.hasErrors()) {
            throw Exception("Mutation error: ${response.errors?.firstOrNull()?.message}")
        }
    }

    /** @see MondayRepository.getAvailableStatuses */
    override suspend fun getAvailableStatuses(boardId: String): List<BoardStatusOption> {
        val response = apolloClient.query(GetBoardColumnsQuery(boardId = listOf(boardId))).execute()

        val columns = response.data?.boards?.firstOrNull()?.columns ?: return emptyList()
        val statusColumn =
            columns.firstOrNull { it?.type?.rawValue == "status" } ?: return emptyList()

        val settingsStr = statusColumn.settings_str
        val settingsJson = Json.parseToJsonElement(settingsStr).jsonObject
        val labelsJson = settingsJson["labels"]?.jsonObject ?: return emptyList()

        return labelsJson.values
            .map { json -> BoardStatusOption(label = json.jsonPrimitive.content) }
            .filter { it.label.isNotBlank() }
    }
}
