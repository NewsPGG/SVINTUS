### Swintus assistant

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
        + DrawPile drawPile
        + DiscardPile discardPile
        + int currentPlayerIndex
        + int turnNumber
        + GameStatus status
        + Card topCard
        + applyTurn(turn) GameState
        + getCurrentPlayer() InGamePlayer
    }

    class InGamePlayer {
        + UUID playerId
        + String name
        + List~Card~ hand
        + boolean declaredSwintus
        + void drawCards(int count, DrawPile pile)
        + boolean canPlayCard(Card topCard)
    }

    class DrawPile {
        - Stack~Card~ cards
        + draw() Card
        + reshuffle(DiscardPile)
    }

    class DiscardPile {
        - Stack~Card~ cards
        + push(card Card)
        + top() Card
    }

    class Card {
        <<abstract>>
        + CardType type
        + Color color
        + int value
    }

    class NumberCard {
        + int number
    }

    class TakeTwoCard { }
    class ReverseCard { }
    class SkipCard { }
    class WildCard { }
    class WildTakeFourCard { }

    Card <|-- NumberCard
    Card <|-- TakeTwoCard
    Card <|-- ReverseCard
    Card <|-- SkipCard
    Card <|-- WildCard
    Card <|-- WildTakeFourCard

    class Turn {
        <<abstract>>
        + UUID gameId
        + int turnNumber
        + UUID playerId
        + LocalDateTime timestamp
    }

    class PlayCardTurn {
        + Card card
        + Color declaredColor
        + boolean declaredSwintus
    }

    class DrawCardTurn {
        + int cardsDrawn
        + Card playedAfterDraw
    }

    class DeclareSwintusTurn {
    }

    Turn <|-- PlayCardTurn
    Turn <|-- DrawCardTurn
    Turn <|-- DeclareSwintusTurn

    class TurnValidator {
        + validate(turn, state) ValidationResult
        - validatePlayCard(turn, state)
        - validateDrawCard(turn, state)
        - validateDeclareSwintus(turn, state)
    }

    class ValidationResult {
        + boolean isValid
        + String errorMessage
    }

    class GameRepository {
        <<interface>>
        + saveGameState(state)
        + loadGameState(gameId)
        + saveTurn(turn)
        + getTurnHistory(gameId)
    }

    class StatsService {
        + updateRatings(gameRecord)
        + getLeaderboard()
    }

    class PlayerProfile {
        + UUID id
        + String username
        + int rating
        + int gamesPlayed
        + int wins
    }

    GameAdministrator "1" --> "1" GameState
    GameAdministrator "1" --> "1" TurnValidator
    GameAdministrator "1" --> "1" GameRepository
    GameAdministrator "1" --> "1" StatsService

    GameState "1" *-- "*" InGamePlayer
    GameState "1" *-- "1" DrawPile
    GameState "1" *-- "1" DiscardPile

    InGamePlayer "1" *-- "*" Card

    TurnValidator ..> Turn : validates
    Turn --> GameState : applied to

    StatsService ..> GameRepository : uses
    StatsService ..> PlayerProfile : updates