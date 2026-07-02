package Cards.Effects

import Game.GameState
import Game.InGamePlayer

class TakeTwoEffect : Effect {
    override fun execute(player: InGamePlayer, gameState: GameState) {
        repeat(2) {
            if (gameState.giveCard.cards.isNotEmpty()) {
                player.hand.add(gameState.giveCard.draw())
            }
        }
        gameState.skipNextTurn = true
    }
}