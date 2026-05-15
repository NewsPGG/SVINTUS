package Game

import Cards.Card
import java.util.UUID

data class GameState (
    var gameId: UUID,
    var players: MutableList<InGamePlayer>,
    var giveCard: GiveCard,
    var discardCard: DiscardCard,
    var currentPlayerIndex: Int,
    var turnNumber: Int,
    var gameStatus: Boolean,
    var topCard: Card,
    var direction: Boolean = true
)