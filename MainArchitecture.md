classDiagram
    class GameAdministrator {
        - GameState currentState
        - IGameStateLogic stateLogic
        - TurnValidator validator
        - GameRepository repository
        - StatsService statsService
        + startNewGame(players) GameState
        + processTurn(turn) ValidationResult
        + undoLastTurn() GameState
        + getHistory() List
    }

    class GameState {
        + UUID gameId
        + List players
        + GiveCard giveCard
        + DiscardCard discardCard
        + int currentPlayerIndex
        + int turnNumber
        + GameStatus status
        + Card topCard
    }

    class IGameStateLogic {
        <<interface>>
        + applyTurn(turn, GameState) GameState
        + getCurrentPlayer(GameState) InGamePlayer
    }

    class PlayerProfile {
        + UUID id
        + String username
        + int rating
        + int gamesPlayed
        + int wins
    }

    class TurnValidator {
        <<interface>>
        + validate(turn, state) ValidationResult
    }

    class GameRepository {
        <<interface>>
        + saveGameState(state)
        + loadGameState(gameId)
        + saveTurn(turn)
        + getTurnHistory(gameId)
        + getAllGameRecords()
    }

    class StatsService {
        <<interface>>
        + updateRatings(gameRecord)
        + getLeaderboard()
    }

    GameAdministrator --> GameState
    GameAdministrator --> IGameStateLogic
    GameAdministrator --> TurnValidator
    GameAdministrator --> GameRepository
    GameAdministrator --> StatsService

    StatsService ..> GameRepository
    StatsService ..> PlayerProfile

    GameState --> PlayerProfile
    IGameStateLogic ..> GameState