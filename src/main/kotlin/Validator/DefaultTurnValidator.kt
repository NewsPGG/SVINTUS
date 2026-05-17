package Validator

import Cards.Types.ActionCard
import Cards.Types.NumberCard
import Cards.Types.WildCard
import Game.GameState
import Validator.TurnActions.DeclareSwintusTurn
import Validator.TurnActions.DrawCardTurn
import Validator.TurnActions.PlayCardTurn
import Validator.TurnActions.Turn

class DefaultTurnValidator : TurnValidator {

    private fun checkPlayCardTurn(playCardTurn: PlayCardTurn, gameState: GameState): ValidationResult {
        val currentTableColor = if (gameState.topCard is WildCard) {
            (gameState.topCard as WildCard).chosenColor
        } else {
            gameState.topCard.color
        }

        val isCardMatch = when {
            playCardTurn.card is WildCard -> true
            playCardTurn.card.color == currentTableColor -> true
            (playCardTurn.card is ActionCard) && (gameState.topCard is ActionCard) &&
                    (playCardTurn.card.effect::class == (gameState.topCard as ActionCard).effect::class) -> true
            (playCardTurn.card is NumberCard) && (gameState.topCard is NumberCard) &&
                    (playCardTurn.card.value == (gameState.topCard as NumberCard).value) -> true
            else -> false
        }

        if (!isCardMatch) {
            return ValidationResult(false, "Карта не может быть сброшена!")
        }

        return ValidationResult(true, null)
    }

    private fun checkDrawCardTurn(drawCardTurn: DrawCardTurn, gameState: GameState): ValidationResult {
        if (drawCardTurn.playerId.canPlayCard(gameState.topCard)) {
            return ValidationResult(false, "У Вас в руках есть подходящая карта!")
        }

        if (drawCardTurn.cardsDrawn != 1) {
            return ValidationResult(false, "Можно взять только одну карту за раз!")
        }

        return ValidationResult(true, null)
    }

    private fun checkDeclareSwintusTurn(turn: DeclareSwintusTurn, gameState: GameState): ValidationResult {
        if (turn.playerId.hand.size <= 2) {
            return ValidationResult(true, null)
        }
        return ValidationResult(false, "Слишком рано кричать 'Свинтус!'")
    }

    override fun validate(turn: Turn, gameState: GameState): ValidationResult {
        if (turn is PlayCardTurn && turn.card is WildCard) {
            return ValidationResult(true, null)
        }

        return when (turn) {
            is PlayCardTurn -> checkPlayCardTurn(turn, gameState)
            is DrawCardTurn -> checkDrawCardTurn(turn, gameState)
            is DeclareSwintusTurn -> checkDeclareSwintusTurn(turn, gameState)
            else -> ValidationResult(false, "Неизвестный ход")
        }
    }
}