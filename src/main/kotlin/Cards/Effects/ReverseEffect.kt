package Cards.Effects

import Game.GameState
import Game.InGamePlayer

class ReverseEffect : Effect {
    override fun execute(player: InGamePlayer, gameState: GameState) {
        if (gameState.players.size == 2) {
            // На двоих игроков Перехрюк работает как Захрапин
            gameState.skipNextTurn = true
        } else {
            // В большой компании просто меняет направление числового ряда
            gameState.direction = !gameState.direction
        }
    }
}