package Validator.TurnActions

import Game.InGamePlayer
import java.util.UUID

class DeclareSwintusTurn (
    override val gameId: UUID,
    override val turnNumber: Int,
    override val playerId: InGamePlayer,
) : Turn(gameId, turnNumber, playerId)