classDiagram
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

    class InGamePlayer {
        + UUID playerId
        + String name
        + List~Card~ hand
        + boolean declaredSwintus
        + drawCards(count,giveCard)
        + canPlayCard(topCard)
    }

    class GiveCard {
        - Stack~Card~ cards
        + draw() Card
        + reshuffle(DiscardCard)
    }

    class DiscardCard {
        - Stack~Card~ cards
        + push(card)
        + top() Card
    }

    GameState "1" *-- "*" InGamePlayer
    GameState "1" *-- "1" GiveCard
    GameState "1" *-- "1" DiscardCard
    InGamePlayer "1" *-- "*" Card