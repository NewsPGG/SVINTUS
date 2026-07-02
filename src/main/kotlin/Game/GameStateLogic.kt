package Game

import Cards.Card
import Cards.Effects.ReverseEffect
import Cards.Effects.SkipEffect
import Cards.Effects.TakeThreeEffect
import Cards.Effects.TakeTwoEffect
import Cards.Types.*
import Validator.TurnActions.*

class GameStateLogic(private val currentState: GameState) : IGameStateLogic {

    override fun applyTurn(turn: Turn): GameState {
        val player = currentState.players.find { it.playerId == turn.playerId.playerId }
            ?: return currentState

        return when (turn) {
            is PlayCardTurn -> computePlayCardTurn(turn, player)
            is DrawCardTurn -> computeDrawCardTurn(turn, player)
            is DeclareSwintusTurn -> computeDeclareSwintusTurn(turn, player)
            else -> currentState
        }
    }

    override fun getCurrentPlayer(gameState: GameState): InGamePlayer {
        return gameState.players[gameState.currentPlayerIndex]
    }

    private fun computePlayCardTurn(turn: PlayCardTurn, player: InGamePlayer): GameState {
        var workingHand = player.hand.toMutableList()

        if (turn.declaredSwintus && workingHand.size > 2) {
            workingHand = applyPenaltyToHand(workingHand, 3)
            return advanceTurnInState(currentState.copy(
                players = updatePlayersInList(currentState.players, player.playerId, workingHand)
            ), stepsToSkip = 0)
        }

        val cardInHand = workingHand.find { it::class == turn.card::class && it.color == turn.card.color &&
                (it !is NumberCard || (turn.card is NumberCard && it.value == turn.card.value)) }
        if (cardInHand != null) workingHand.remove(cardInHand) else workingHand.remove(turn.card)

        if (workingHand.isEmpty()) {
            currentState.discardCard.push(currentState.topCard)
            val playedCard = turn.card
            if (playedCard is WildCard) {
                playedCard.chosenColor = turn.declaredColor
            }
            return currentState.copy(
                players = updatePlayersInList(currentState.players, player.playerId, workingHand),
                topCard = playedCard,
                gameStatus = false
            )
        }

        val playedCard = turn.card
        if (playedCard is WildCard) {
            playedCard.chosenColor = turn.declaredColor
        }

        currentState.discardCard.push(currentState.topCard)
        var nextState = currentState.copy(
            players = updatePlayersInList(currentState.players, player.playerId, workingHand),
            topCard = playedCard
        )

        var stepsToSkip = 0
        if (playedCard is ActionCard) {
            val effect = playedCard.effect
            val size = nextState.players.size
            val step = if (nextState.direction) 1 else -1
            val nextPlayerIndex = (nextState.currentPlayerIndex + step + size) % size
            val targetPlayer = nextState.players[nextPlayerIndex]

            var targetHand = targetPlayer.hand.toMutableList()

            when (effect) {
                is SkipEffect -> stepsToSkip = 1
                is TakeThreeEffect -> {
                    targetHand = applyPenaltyToHand(targetHand, 3)
                    nextState = nextState.copy(players = updatePlayersInList(nextState.players, targetPlayer.playerId, targetHand))
                    stepsToSkip = 1
                }
                is TakeTwoEffect -> {
                    targetHand = applyPenaltyToHand(targetHand, 2)
                    nextState = nextState.copy(players = updatePlayersInList(nextState.players, targetPlayer.playerId, targetHand))
                    stepsToSkip = 1
                }
                is ReverseEffect -> {
                    nextState = nextState.copy(direction = !nextState.direction)
                    stepsToSkip = if (size == 2) 1 else 0
                }
            }
        }

        return advanceTurnInState(nextState, stepsToSkip)
    }

    private fun computeDrawCardTurn(turn: DrawCardTurn, player: InGamePlayer): GameState {
        ensureDeckIsNotEmpty()

        if (currentState.giveCard.cards.isNotEmpty()) {
            val workingHand = player.hand.toMutableList()
            workingHand.add(currentState.giveCard.draw(currentState.discardCard))

            return currentState.copy(
                players = updatePlayersInList(currentState.players, player.playerId, workingHand),
                turnNumber = currentState.turnNumber + 1
            )
        }
        return advanceTurnInState(currentState, stepsToSkip = 0)
    }

    private fun computeDeclareSwintusTurn(turn: DeclareSwintusTurn, player: InGamePlayer): GameState {
        val updatedPlayers = currentState.players.map { p ->
            if (p.playerId == player.playerId) {
                p.declaredSwintus = true
            }
            p
        }.toMutableList()
        return currentState.copy(players = updatedPlayers)
    }

    private fun ensureDeckIsNotEmpty() {
        if (currentState.giveCard.cards.isEmpty()) {
            val discardedPile = currentState.discardCard.cards
            if (discardedPile.size > 0) {
                val recycledCards = mutableListOf<Card>()
                while (discardedPile.size > 0) {
                    recycledCards.add(discardedPile.pop())
                }
                recycledCards.shuffle()
                currentState.giveCard.cards.addAll(recycledCards)
            }
        }
    }

    private fun applyPenaltyToHand(hand: List<Card>, count: Int): MutableList<Card> {
        val newHand = hand.toMutableList()
        repeat(count) {
            ensureDeckIsNotEmpty()
            if (currentState.giveCard.cards.isNotEmpty()) {
                newHand.add(currentState.giveCard.draw(currentState.discardCard))
            }
        }
        return newHand
    }

    private fun updatePlayersInList(players: List<InGamePlayer>, id: java.util.UUID, newHand: List<Card>): MutableList<InGamePlayer> {
        return players.map { p ->
            if (p.playerId == id) {
                p.hand = newHand.toMutableList()
            }
            p
        }.toMutableList()
    }

    private fun advanceTurnInState(state: GameState, stepsToSkip: Int): GameState {
        val size = state.players.size
        if (size == 0) return state

        val step = if (state.direction) 1 else -1
        val totalSteps = 1 + stepsToSkip
        val newPlayerIndex = (state.currentPlayerIndex + (step * totalSteps) + size * totalSteps) % size

        return state.copy(
            currentPlayerIndex = newPlayerIndex,
            turnNumber = state.turnNumber + 1
        )
    }
}