package Cards.Effects

import Game.GameState
import Game.InGamePlayer

class TakeThreeEffect: Effect {
    override fun execute(player: InGamePlayer, gameState: GameState) {
        player.drawCards(3, gameState.giveCard)
    }
}