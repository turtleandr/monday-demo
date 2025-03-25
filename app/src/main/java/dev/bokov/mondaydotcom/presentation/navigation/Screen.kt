package dev.bokov.mondaydotcom.presentation.navigation

/**
 * Represents a screen in the app
 *
 * @param route The route of the screen
 */
sealed class Screen(val route: String) {

    data object Boards : Screen("boards")

    data class Tasks(val boardId: String, val boardName: String) :
        Screen("tasks/{$KEY_BOARD_ID}/{$KEY_BOARD_NAME}") {

        companion object {
            const val KEY_BOARD_ID = "boardId"
            const val KEY_BOARD_NAME = "boardName"

            fun createRoute(boardId: String, boardName: String) = "tasks/$boardId/$boardName"
        }
    }
}
