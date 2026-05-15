package Game

import Validator.TurnActions.Turn

interface IGameStateLogic {
    fun applyTurn(turn: Turn): GameState

    fun getCurrentPlayer(gameState: GameState): InGamePlayer
}