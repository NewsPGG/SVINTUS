package Cards.Effects

import Game.GameState
import Game.InGamePlayer

interface Effect {
    fun execute(player: InGamePlayer, gameState: GameState)
}