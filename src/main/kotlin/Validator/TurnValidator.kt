package Validator

import Game.GameState
import Validator.TurnActions.Turn

interface TurnValidator {
    fun validate(turn: Turn, gameState: GameState): ValidationResult
}