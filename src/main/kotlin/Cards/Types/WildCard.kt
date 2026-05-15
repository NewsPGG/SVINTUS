package Cards.Types

import Cards.Card
import Game.Color
import Game.GameState
import Game.InGamePlayer

class WildCard (
    override var color: Color,
    var chosenColor: Color
) : Card() {

    override fun applyEffect(player: InGamePlayer, gameState: GameState) {
        gameState.topCard.color = this.chosenColor
    }
}