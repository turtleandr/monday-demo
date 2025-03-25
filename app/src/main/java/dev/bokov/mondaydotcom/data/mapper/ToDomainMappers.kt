package dev.bokov.mondaydotcom.data.mapper

import dev.bokov.mondaydotcom.domain.model.Board
import dev.bokov.mondaydotcom.domain.model.BoardItem
import dev.bokov.mondaydotcom.domain.model.BoardItemColumn
import dev.bokov.mondaydotcom.domain.model.BoardWithTasks
import dev.bokov.mondaydotcom.graphql.GetBoardItemsQuery
import dev.bokov.mondaydotcom.graphql.GetBoardsQuery

internal fun GetBoardItemsQuery.Board.toDomain(): BoardWithTasks {
    return BoardWithTasks(
        id = this.id,
        name = this.name,
        items = this.items_page.items.map { it.toDomain() },
    )
}

internal fun GetBoardItemsQuery.Item.toDomain(): BoardItem {
    return BoardItem(
        id = this.id,
        name = this.name,
        columnValues =
            this.column_values.map { col ->
                col.let { BoardItemColumn(id = it.id, text = it.text) }
            },
    )
}

internal fun GetBoardsQuery.Board.toDomain(): Board {
    val id = this.id
    val name = this.name
    return Board(id = id, name = name)
}
