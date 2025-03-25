package dev.bokov.mondaydotcom.domain.model

internal data class BoardItem(
    val id: String,
    val name: String,
    val columnValues: List<BoardItemColumn>
)