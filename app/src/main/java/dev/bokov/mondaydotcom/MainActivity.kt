package dev.bokov.mondaydotcom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.bokov.mondaydotcom.presentation.navigation.MondayNavGraph
import dev.bokov.mondaydotcom.presentation.ui.theme.MondayDotComTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MondayDotComTheme {
                Surface(modifier = Modifier) {
                    val navController = rememberNavController()
                    MondayNavGraph(navController = navController)
                }
            }
        }
    }
}
