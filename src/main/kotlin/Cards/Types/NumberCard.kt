package Cards.Types

import Cards.Card
import Game.Color
import Game.GameState
import Game.InGamePlayer

class NumberCard (
    override var color: Color,
    val value: Int
) : Card() {

    override fun applyEffect(player: InGamePlayer, gameState: GameState) {}
}