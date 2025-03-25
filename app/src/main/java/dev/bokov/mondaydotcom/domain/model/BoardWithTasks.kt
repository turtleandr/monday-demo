package dev.bokov.mondaydotcom.domain.model

internal data class BoardWithTasks(
    val id: String,
    val name: String,
    val items: List<BoardItem>
)
