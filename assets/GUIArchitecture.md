classDiagram
    direction TB

    class Application {
        +main()
    }

    class LocalPlayerDataBase {
        +List~PlayerProfile~ players
    }

    class SwintusViewModel {
        +MutableStateFlow gameState
        +MutableStateFlow stateVersion
        +SnapshotStateList actionLogs
        +startGameFromLobby(initialState)
        +handlePlayCardFromId(playerId, card, shoutedSwintus)
        +handleDrawCard()
        +handleAccusePlayer(targetPlayerId, UIcurrentPlayerName)
    }

    class LobbyScreen {
        <<Composable>>
        +SwintusViewModel viewModel
    }

    class GameScreen {
        <<Composable>>
        +SwintusViewModel viewModel
    }

    class CardView {
        <<Composable>>
        +Card card
        +onClick()
    }

    Application --> LocalPlayerDataBase
    Application --> SwintusViewModel
    
    LobbyScreen ..> SwintusViewModel
    GameScreen ..> SwintusViewModel
    
    GameScreen --> CardView