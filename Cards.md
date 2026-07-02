classDiagram
    class Card {
       <<abstract>>
        + color: Color
        + effect: Effect?
        + applyEffect(player, gameState)
    }

    class NumberCard {
        + value: int
    }

    class WildCard {
        + chosenColor: Color
    }

    class Effect {
        <<interface>>
        + execute(player, gameState)
    }

    class TakeTwoEffect {
        + execute(player, gameState)