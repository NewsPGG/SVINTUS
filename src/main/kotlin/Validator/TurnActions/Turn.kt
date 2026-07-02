package Validator.TurnActions

import Game.InGamePlayer
import java.util.UUID

abstract class Turn (
    open val gameId: UUID,
    open val turnNumber: Int,
    open val playerId: InGamePlayer
)
