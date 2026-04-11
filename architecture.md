classDiagram
%% --- Интерфейсы ---
interface TurnValidator {
+ validate(turn: Turn, state: GameState): ValidationResult
}

    interface StatsService {
        + updateRatings(gameRecord: GameRecord): List~PlayerProfile~
        + getLeaderboard(): List~PlayerProfile~
        + computePlayerStatistics(playerId: UUID): PlayerProfile
    }

    interface GameRepository {
        + saveGameState(state: GameState)
        + loadGameState(gameId: UUID): GameState?
        + saveTurn(turn: Turn)
        + getTurnHistory(gameId: UUID): List~Turn~
        + getAllGameRecords(): List~GameRecord~
    }

    interface GameAdministrator {
        + startNewGame(players: List~PlayerProfile~): GameState
        + processTurn(turn: Turn): ValidationResult
        + undoLastTurn(): GameState
        + getHistory(): List~Turn~
    }

    interface Pile {
        + draw(): Card
        + push(card: Card)
        + top(): Card
        + isEmpty(): Boolean
        + reshuffleFrom(pile: Pile)
    }

    interface CardMatcher {
        + matches(card: Card, topCard: Card, calledColor: Color?): Boolean
    }

    %% --- Реализации (конкретные классы) ---
    class DefaultGameAdministrator {
        - GameState currentState
        - TurnValidator validator
        - GameRepository repository
        - StatsService statsService
        + startNewGame(...)
        + processTurn(...)
        + undoLastTurn()
        + getHistory()
    }
    DefaultGameAdministrator ..|> GameAdministrator

    class StandardSwintusValidator {
        - CardMatcher cardMatcher
        + validate(turn, state)
        - validatePlayCard(...)
        - validateDrawCard(...)
        - validateDeclareSwintus(...)
    }
    StandardSwintusValidator ..|> TurnValidator

    class InMemoryStatsService {
        - MutableMap~UUID, PlayerProfile~ profiles
        - MutableList~GameRecord~ games
        + updateRatings(...)
        + getLeaderboard()
        + computePlayerStatistics(...)
    }
    InMemoryStatsService ..|> StatsService

    class FileGameRepository {
        - String storagePath
        + saveGameState(...)
        + loadGameState(...)
        + saveTurn(...)
        + getTurnHistory(...)
        + getAllGameRecords(...)
    }
    FileGameRepository ..|> GameRepository

    class InMemoryGameRepository {
        - MutableMap~UUID, GameState~ states
        - MutableMap~UUID, MutableList~Turn~~ history
        + ... реализации
    }
    InMemoryGameRepository ..|> GameRepository

    class DrawPile {
        - Stack~Card~ cards
        + draw()
        + push(card) // не используется напрямую
        + top()
        + isEmpty()
        + reshuffleFrom(pile: Pile)
    }
    DrawPile ..|> Pile

    class DiscardPile {
        - Stack~Card~ cards
        + draw() // обычно не используется
        + push(card)
        + top()
        + isEmpty()
        + reshuffleFrom(pile) // не нужно для сброса
    }
    DiscardPile ..|> Pile

    class DefaultCardMatcher {
        + matches(card, topCard, calledColor)
    }
    DefaultCardMatcher ..|> CardMatcher

    %% --- Остальные классы без изменений ---
    class GameState { ... }
    class InGamePlayer { ... }
    class Turn { ... }
    class PlayCardTurn { ... }
    class DrawCardTurn { ... }
    class DeclareSwintusTurn { ... }
    class ValidationResult { ... }
    class Card { ... }
    class NumberCard { ... }
    class TakeTwoCard { ... }
    class ReverseCard { ... }
    class SkipCard { ... }
    class WildCard { ... }
    class WildTakeFourCard { ... }
    class PlayerProfile { ... }
    class GameRecord { ... }
    class PlayerResult { ... }

    %% --- Зависимости (через интерфейсы) ---
    DefaultGameAdministrator --> TurnValidator : depends on
    DefaultGameAdministrator --> GameRepository : depends on
    DefaultGameAdministrator --> StatsService : depends on

    StandardSwintusValidator --> CardMatcher : uses

    GameState *-- DrawPile
    GameState *-- DiscardPile
    GameState *-- InGamePlayer

    TurnValidator ..> Turn : validates
    Turn --> GameState : applied to