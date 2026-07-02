package Cards

import Game.Color
import Game.GameState
import Game.InGamePlayer

abstract class Card() {
    abstract var color: Color

    abstract fun applyEffect(player: InGamePlayer, gameState: GameState)
}