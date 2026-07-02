package Cards.Effects

import Game.GameState
import Game.InGamePlayer

class ReverseEffect : Effect {
    override fun execute(player: InGamePlayer, gameState: GameState) {
        if (gameState.players.size == 2) {
            gameState.skipNextTurn = true
        } else {
            gameState.direction = !gameState.direction
        }
    }
}