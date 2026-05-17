package view

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import view.screens.GameScreen
import view.screens.LobbyScreen
import viewmodel.SwintusViewModel

@Composable
fun Application() {
    val viewModel = remember { SwintusViewModel() }
    val gameState by viewModel.gameState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.onResetToLobby = {
            viewModel.gameState.value = null
        }
    }

    MaterialTheme {
        if (gameState == null) {
            LobbyScreen(viewModel = viewModel)
        } else {
            GameScreen(viewModel = viewModel)
        }
    }
}