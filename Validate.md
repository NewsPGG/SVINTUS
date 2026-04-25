classDiagram
class Turn {
<<abstract>>
+ UUID gameId
+ int turnNumber
+ UUID playerId
+ LocalDateTime timestamp
}

    class PlayCardTurn {
        + Card cardcg
        + Color declaredColor
        + boolean declaredSwintus
    }

    class DrawCardTurn {
        + int cardsDrawn
        + Card playedAfterDraw
    }

    class DeclareSwintusTurn {
    }

    class ValidationResult {
        + boolean isValid
        + String errorMessage
    }

    Turn <|-- PlayCardTurn
    Turn <|-- DrawCardTurn
    Turn <|-- DeclareSwintusTurn

    interface TurnValidator {
        + validate(turn,state) ValidationResult
    }

    TurnValidator ..> Turn : validates
    TurnValidator ..> ValidationResult : returns