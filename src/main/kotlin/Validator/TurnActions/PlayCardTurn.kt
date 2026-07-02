package Validator.TurnActions

import Cards.Card
import Game.Color
import Game.InGamePlayer
import java.util.UUID

class PlayCardTurn (
    override val gameId: UUID,
    override val turnNumber: Int,
    override val playerId: InGamePlayer,
    val card: Card,
    val declaredColor: Color,
    val declaredSwintus: Boolean
) : Turn(gameId, turnNumber, playerId)