package Validator

import Cards.Card
import Cards.Types.ActionCard
import Cards.Types.NumberCard
import Cards.Types.WildCard
import Game.Color
import Game.GameState
import Validator.TurnActions.DeclareSwintusTurn
import Validator.TurnActions.DrawCardTurn
import Validator.TurnActions.PlayCardTurn
import Validator.TurnActions.Turn

class DefaultTurnValidator : TurnValidator {
    private fun checkPlayCardTurn(playCardTurn: PlayCardTurn, gameState: GameState): ValidationResult {
        val isCardMatch: Boolean

        if (playCardTurn.card is WildCard) {
            isCardMatch = true
        } else if (playCardTurn.card.color == gameState.topCard.color) {
            isCardMatch = true
        } else if ((playCardTurn.card is ActionCard) && (gameState.topCard is ActionCard) &&
            (playCardTurn.card.effect == (gameState.topCard as ActionCard).effect)
        ) {
            isCardMatch = true
        } else if ((playCardTurn.card is NumberCard) && (gameState.topCard is NumberCard) &&
            (playCardTurn.card.value == (gameState.topCard as NumberCard).value)
        ) {
            isCardMatch = true
        } else {
            return ValidationResult(false, "Карта не может быть сброшена!")
        }

        val numberCards: Int = playCardTurn.playerId.hand.size

        return when (numberCards) {
            1 -> if (playCardTurn.declaredSwintus) {
                ValidationResult(true, null)
            } else {
                ValidationResult(false, "Вы не крикнули 'Свинтус!'")
            }
            else -> if (!playCardTurn.declaredSwintus) {
                ValidationResult(true, null)
            } else {
                ValidationResult(false, "У Вас более одной карты!")
            }
        }
    }

    private fun hasPlayableCard(hand: List<Card>, topCard: Card): Boolean {
        for (card in hand) {
            if (card.color == Color.GRAY || card.color == topCard.color) {
                return true
            }

            if (card is NumberCard && topCard is NumberCard) {
                return card.value == topCard.value
            }

            if (card is ActionCard && topCard is ActionCard) {
                return card.effect == topCard.effect
            }
        }
        return false
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
        val expectedPlayer = gameState.players[gameState.currentPlayerIndex]
        if (turn.playerId.playerId != expectedPlayer.playerId) {
            return ValidationResult(false, "Сейчас не ваш ход!")
        }

        return when (turn) {
            is PlayCardTurn -> checkPlayCardTurn(turn, gameState)
            is DrawCardTurn -> checkDrawCardTurn(turn, gameState)
            is DeclareSwintusTurn -> checkDeclareSwintusTurn(turn, gameState)
            else -> ValidationResult(false, "Неизвестный тип хода!")
        }
    }
}