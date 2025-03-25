package dev.bokov.mondaydotcom.domain.model

internal data class Task(
    val id: String,
    val name: String,
    val status: String,
    val columnId: String,
    val boardId: String
)