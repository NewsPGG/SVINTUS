# Диаграммы классов для игры «Свинтус»

```mermaid
classDiagram
    class GameAdministrator {
        - GameState currentState
        - TurnValidator validator
        - GameRepository repository
        - StatsService statsService
        + startNewGame(players) GameState
        + processTurn(turn) ValidationResult
        + undoLastTurn() GameState
        + getHistory() List~Turn~
    }

    class GameState {
        + UUID gameId
        + List~InGamePlayer~ players
        + GiveCard giveCard
        + DiscardCard discardCard
        + int currentPlayerIndex
        + int turnNumber
        + GameStatus status
        + Card topCard
        + applyTurn(turn) GameState
        + getCurrentPlayer() InGamePlayer
    }

    class PlayerProfile {
        + UUID id
        + String username
        + int rating
        + int gamesPlayed
        + int wins
    }

    interface TurnValidator {
        + validate(turn,state) ValidationResult
    }

    interface GameRepository {
        + saveGameState(state)
        + loadGameState(gameId)
        + saveTurn(turn)
        + getTurnHistory(gameId)
        + getAllGameRecords()
    }

    interface StatsService {
        + updateRatings(gameRecord)
        + getLeaderboard()
    }

    GameAdministrator "1" --> "1" GameState
    GameAdministrator "1" --> "1" TurnValidator
    GameAdministrator "1" --> "1" GameRepository
    GameAdministrator "1" --> "1" StatsService

    StatsService ..> GameRepository : uses
    StatsService ..> PlayerProfile : updates
    GameState --> PlayerProfile : refers via InGamePlayer