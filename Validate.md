classDiagram
class Turn {
<<abstract>>
+gameId
+turnNumber
+playerId
+timestamp
}

    class PlayCardTurn {
        +card
        +declaredColor
        +declaredSwintus
    }

    class DrawCardTurn {
        +cardsDrawn
        +playedAfterDraw
    }

    class DeclareSwintusTurn {
    }

    class ValidationResult {
        +isValid
        +errorMessage
    }

    Turn <|-- PlayCardTurn
    Turn <|-- DrawCardTurn
    Turn <|-- DeclareSwintusTurn

    class TurnValidator {
        <<interface>>
        +validate(turn,state)
    }

    TurnValidator ..> Turn : validates
    TurnValidator ..> ValidationResult : returns