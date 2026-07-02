package Cards.Types

import Cards.Card
import Cards.Effects.Effect
import Game.Color
import Game.GameState
import Game.InGamePlayer

class ActionCard (
    override var color: Color,
    val effect: Effect
) : Card() {

    override fun applyEffect(player: InGamePlayer, gameState: GameState) {
        effect.execute(player, gameState)
    }
}