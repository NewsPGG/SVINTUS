package Game

import Cards.Types.*
import Validator.TurnActions.*

class GameStateLogic(private var gameState: GameState) : IGameStateLogic {

    override fun applyTurn(turn: Turn): GameState {
        val player = gameState.players.find { it.playerId == turn.playerId.playerId }
            ?: return gameState

        when (turn) {
            is PlayCardTurn -> applyPlayCardTurn(turn, player)
            is DrawCardTurn -> applyDrawCardTurn(turn, player)
            is DeclareSwintusTurn -> applyDeclareSwintusTurn(turn, player)
        }

        return gameState
    }

    override fun getCurrentPlayer(gameState: GameState): InGamePlayer {
        return gameState.players[gameState.currentPlayerIndex]
    }

    private fun applyPlayCardTurn(turn: PlayCardTurn, player: InGamePlayer) {
        if (turn.declaredSwintus && player.hand.size > 2) {
            applyPenalty(player, 3)
            advanceTurn()
            return
        }

        if (!turn.declaredSwintus && player.hand.size == 2) {
            applyPenalty(player, 2)
        }

        player.hand.remove(turn.card)

        if (turn.card is WildCard) {
            turn.card.chosenColor = turn.declaredColor
        }

        gameState.discardCard.push(gameState.topCard)
        gameState.topCard = turn.card

        val indexBefore = gameState.currentPlayerIndex
        val directionBefore = gameState.direction

        turn.card.applyEffect(player, gameState)

        if (player.hand.isEmpty()) {
            gameState.gameStatus = false
        } else {
            val indexAfter = gameState.currentPlayerIndex
            val directionAfter = gameState.direction

            if (indexAfter == indexBefore && directionAfter == directionBefore) {
                advanceTurn()
            } else {
                gameState.turnNumber += 1
            }
        }
    }

    private fun applyDrawCardTurn(turn: DrawCardTurn, player: InGamePlayer) {
        repeat(turn.cardsDrawn) {
            if (isDeckReady()) {
                player.hand.add(gameState.giveCard.draw())
            }
        }
    }

    private fun applyDeclareSwintusTurn(turn: DeclareSwintusTurn, player: InGamePlayer) {
        player.declaredSwintus = true
    }

    private fun applyPenalty(player: InGamePlayer, count: Int) {
        repeat(count) {
            if (isDeckReady()) {
                player.hand.add(gameState.giveCard.draw())
            }
        }
    }

    private fun isDeckReady(): Boolean = gameState.giveCard.cards.isNotEmpty()

    private fun advanceTurn() {
        val size = gameState.players.size
        if (size == 0) return
        val step = if (gameState.direction) 1 else -1
        gameState.currentPlayerIndex = (gameState.currentPlayerIndex + step + size) % size
        gameState.turnNumber += 1
    }
}