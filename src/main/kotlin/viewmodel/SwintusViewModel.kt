package viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import Game.GameState
import Game.GameStateLogic
import Cards.Card
import Cards.Types.ActionCard
import Cards.Types.NumberCard
import Cards.Types.WildCard
import Validator.TurnActions.PlayCardTurn
import Validator.TurnActions.DrawCardTurn
import java.util.UUID

class SwintusViewModel {

    val gameState = MutableStateFlow<GameState?>(null)
    val stateVersion = MutableStateFlow(0)
    val actionLogs = mutableStateListOf<String>("Игра началась.")
    private val forgottenSwintusTurns = mutableStateMapOf<UUID, Int>()

    var onResetToLobby: (() -> Unit)? = null

    fun startGameFromLobby(initialState: GameState) {
        actionLogs.clear()
        actionLogs.add("Игра успешно запущена!")
        gameState.value = initialState
        stateVersion.value += 1
    }

    fun handlePlayCardFromId(playerId: UUID, card: Card, shoutedSwintus: Boolean): String? {
        val state = gameState.value ?: return "Игра не запущена"

        val clickedPlayer = state.players.find { it.playerId == playerId }
            ?: return "Игрок не найден"

        val clickedPlayerIndex = state.players.indexOf(clickedPlayer)

        if (clickedPlayerIndex != state.currentPlayerIndex) {
            val currentName = state.players.getOrNull(state.currentPlayerIndex)?.name ?: "другого игрока"
            return "Сейчас ход игрока $currentName!"
        }

        val hasCard = clickedPlayer.hand.any { it::class == card::class && it.color == card.color &&
                (it !is NumberCard || (card is NumberCard && it.value == card.value)) }

        if (!hasCard) {
            return "У вас нет этой карты!"
        }

        if (!card.canPlayOn(state.topCard, clickedPlayer, state)) {
            return "Эту карту нельзя выложить на ${getCardName(state.topCard)}!"
        }

        val handSizeBefore = clickedPlayer.hand.size
        val cardNameReport = getCardName(card)
        val finalDeclaredColor = if (card is WildCard) card.chosenColor else card.color

        val turn = PlayCardTurn(
            gameId = state.gameId,
            turnNumber = state.turnNumber,
            playerId = clickedPlayer,
            card = card,
            declaredSwintus = shoutedSwintus,
            declaredColor = finalDeclaredColor
        )

        val engine = GameStateLogic(state)
        val updatedState = engine.applyTurn(turn)

        if (handSizeBefore == 2 && !shoutedSwintus) {
            forgottenSwintusTurns[clickedPlayer.playerId] = state.turnNumber
        }

        actionLogs.add("${clickedPlayer.name} сыграл карту: $cardNameReport")

        gameState.value = updatedState
        stateVersion.value += 1
        return null
    }

    fun handleDrawCard(): String? {
        val state = gameState.value ?: return "Игра не запущена"

        val size = state.players.size
        val safeIndex = ((state.currentPlayerIndex % size) + size) % size
        val currentPlayer = state.players[safeIndex]

        val hasValidCardInHand = currentPlayer.hand.any { card ->
            card.canPlayOn(state.topCard, currentPlayer, state)
        }

        if (hasValidCardInHand) {
            return "У вас уже есть подходящая карта для хода! Вы обязаны скинуть её."
        }

        val turn = DrawCardTurn(
            gameId = state.gameId,
            turnNumber = state.turnNumber,
            playerId = currentPlayer,
            cardsDrawn = 1,
            playedAfterDraw = false
        )

        val engine = GameStateLogic(state)
        val updatedState = engine.applyTurn(turn)

        actionLogs.add("${currentPlayer.name} взял карту из колоды.")

        gameState.value = updatedState
        stateVersion.value += 1
        return null
    }

    fun handleAccusePlayer(targetPlayerId: UUID, UIcurrentPlayerName: String): String? {
        val state = gameState.value ?: return "Игра не запущена"
        val violationTurn = forgottenSwintusTurns[targetPlayerId]

        val targetPlayer = state.players.find { it.playerId == targetPlayerId }
            ?: return "Игрок не найден"

        if (targetPlayer.hand.size != 1) {
            return "У этого игрока не одна карта!"
        }
        if (violationTurn == null) {
            return "Этот игрок не нарушал правила!"
        }

        val roundsPassed = state.turnNumber - violationTurn
        if (roundsPassed > state.players.size) {
            forgottenSwintusTurns.remove(targetPlayerId)
            return "Время обвинения истекло! Прошел целый круг."
        }

        val updatedHand = targetPlayer.hand.toMutableList()

        val engine = GameStateLogic(state)

        repeat(2) {
            if (state.giveCard.cards.isEmpty() && state.discardCard.cards.size > 0) {
                val recycled = mutableListOf<Card>()
                while (state.discardCard.cards.size > 0) {
                    recycled.add(state.discardCard.cards.pop())
                }
                recycled.shuffle()
                state.giveCard.cards.addAll(recycled)
            }

            if (state.giveCard.cards.isNotEmpty()) {
                updatedHand.add(state.giveCard.draw(state.discardCard))
            }
        }

        targetPlayer.hand = updatedHand

        actionLogs.add("Поймали! $UIcurrentPlayerName обвинил ${targetPlayer.name}. +2 карты нарушителю!")
        forgottenSwintusTurns.remove(targetPlayerId)

        stateVersion.value += 1
        return null
    }

    fun resetToLobby() {
        onResetToLobby?.invoke()
    }

    fun getCardName(card: Card): String {
        val colorName = when (card.color) {
            Game.Color.RED -> "Красный"
            Game.Color.GREEN -> "Зеленый"
            Game.Color.BLUE -> "Синий"
            Game.Color.YELLOW -> "Желтый"
            Game.Color.GRAY -> "Серый"
        }

        return when (card) {
            is NumberCard -> "$colorName Цифра ${card.value}"
            is ActionCard -> {
                val effectName = card.effect::class.java.simpleName.replace("Effect", "").uppercase()
                "$colorName [$effectName]"
            }
            is WildCard -> {
                val chosenColorName = if (card.chosenColor != Game.Color.GRAY) {
                    when (card.chosenColor) {
                        Game.Color.RED -> "Красный"
                        Game.Color.GREEN -> "Зеленый"
                        Game.Color.BLUE -> "Синий"
                        Game.Color.YELLOW -> "Желтый"
                        else -> "Серый"
                    }
                } else ""
                "ПОЛИСВИН ${if (chosenColorName.isNotEmpty()) "(Цвет: $chosenColorName)" else ""}".trim()
            }
            else -> card.javaClass.simpleName.replace("Card", "")
        }
    }

    fun Card.canPlayOn(topCard: Card, player: Game.InGamePlayer, state: GameState): Boolean {
        if (this is WildCard || this.color == Game.Color.GRAY) return true

        if (topCard is WildCard) {
            if (this.color == topCard.chosenColor) return true
            return false
        }

        if (this.color == topCard.color) return true
        if (this is NumberCard && topCard is NumberCard && this.value == topCard.value) return true
        if (this is ActionCard && topCard is ActionCard && this.effect::class == topCard.effect::class) return true
        return false
    }
}