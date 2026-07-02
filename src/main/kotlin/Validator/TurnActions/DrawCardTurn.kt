package Validator.TurnActions

import Game.InGamePlayer
import java.util.UUID

class DrawCardTurn (
    override val gameId: UUID,
    override val turnNumber: Int,
    override val playerId: InGamePlayer,
    var cardsDrawn: Int,
    var playedAfterDraw: Boolean
) : Turn(gameId, turnNumber, playerId)