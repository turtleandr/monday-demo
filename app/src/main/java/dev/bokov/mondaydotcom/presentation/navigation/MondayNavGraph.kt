package dev.bokov.mondaydotcom.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.bokov.mondaydotcom.presentation.ui.screens.BoardListScreen
import dev.bokov.mondaydotcom.presentation.ui.screens.BoardTasksScreen

/**
 * App navigation graph
 */
@Composable
fun MondayNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Boards.route) {
        composableWithTransitions(Screen.Boards.route) {
            BoardListScreen(
                onBoardClick = { boardId, boardName ->
                    navController.navigate(Screen.Tasks.createRoute(boardId, boardName))
                }
            )
        }

        composableWithTransitions(
            route = "tasks/{${Screen.Tasks.KEY_BOARD_ID}}/{${Screen.Tasks.KEY_BOARD_NAME}}"
        ) { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString(Screen.Tasks.KEY_BOARD_ID).orEmpty()
            val boardName =
                backStackEntry.arguments?.getString(Screen.Tasks.KEY_BOARD_NAME).orEmpty()

            BoardTasksScreen(
                boardId = boardId,
                boardName = boardName,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

fun NavGraphBuilder.composableWithTransitions(
    route: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        content = content,
    )
}
