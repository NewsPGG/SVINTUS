package viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import Game.GameState
import Game.GameStateLogic
import Cards.Card
import Cards.Types.ActionCard
import Cards.Types.NumberCard
import Cards.Types.WildCard
import Validator.TurnActions.PlayCardTurn
import Validator.TurnActions.DrawCardTurn
import Application.PlayerProfile
import data.repository.GameRepository
import java.util.UUID

class SwintusViewModel(private val repository: GameRepository) {

    val gameState = MutableStateFlow<GameState?>(null)
    val stateVersion = MutableStateFlow(0)
    val actionLogs = mutableStateListOf<String>("Игра началась.")
    private val forgottenSwintusTurns = mutableStateMapOf<UUID, Int>()

    private val viewModelScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var onResetToLobby: (() -> Unit)? = null

    fun startGameFromLobby(initialState: GameState) {
        actionLogs.clear()
        actionLogs.add("Игра успешно запущена!")
        gameState.value = initialState
        updateVersion()
    }

    fun handlePlayCardFromId(playerId: UUID, card: Card, shoutedSwintus: Boolean): String? {
        val state = gameState.value ?: return "Игра не запущена"
        val clickedPlayer = state.players.find { it.playerId == playerId } ?: return "Игрок не найден"

        val validationError = checkTurnAndHand(state, clickedPlayer, card)
        if (validationError != null) return validationError

        val handSizeBefore = clickedPlayer.hand.size
        val finalDeclaredColor = if (card is WildCard) card.chosenColor else card.color

        val turn = PlayCardTurn(
            gameId = state.gameId, turnNumber = state.turnNumber, playerId = clickedPlayer,
            card = card, declaredSwintus = shoutedSwintus, declaredColor = finalDeclaredColor
        )

        gameState.value = GameStateLogic(state).applyTurn(turn)
        checkForgottenSwintus(clickedPlayer.playerId, handSizeBefore, shoutedSwintus, state.turnNumber)

        actionLogs.add("${clickedPlayer.name} сыграл карту: ${getCardName(card)}")
        updateVersion()
        return null
    }

    fun handleDrawCard(): String? {
        val state = gameState.value ?: return "Игра не запущена"
        val currentPlayer = getCurrentPlayerSafely(state)

        if (currentPlayer.hand.any { it.canPlayOn(state.topCard, currentPlayer, state) }) {
            return "У вас уже есть подходящая карта для хода! Вы обязаны скинуть её."
        }

        val turn = DrawCardTurn(
            gameId = state.gameId, turnNumber = state.turnNumber,
            playerId = currentPlayer, cardsDrawn = 1, playedAfterDraw = false
        )

        gameState.value = GameStateLogic(state).applyTurn(turn)
        actionLogs.add("${currentPlayer.name} взял карту из колоды.")
        updateVersion()
        return null
    }

    fun handleAccusePlayer(targetPlayerId: UUID, UIcurrentPlayerName: String): String? {
        val state = gameState.value ?: return "Игра не запущена"
        val targetPlayer = state.players.find { it.playerId == targetPlayerId } ?: return "Игрок не найден"
        val violationTurn = forgottenSwintusTurns[targetPlayerId]

        val validationError = checkAccuseValidity(targetPlayer, violationTurn, state.turnNumber, state.players.size)
        if (validationError != null) return validationError

        executePenaltyDrawn(state, targetPlayer)

        actionLogs.add("Поймали! $UIcurrentPlayerName обвинил ${targetPlayer.name}. +2 карты нарушителю!")
        forgottenSwintusTurns.remove(targetPlayerId)
        updateVersion()
        return null
    }

    fun handleGameEnd(winnerId: UUID, winnerName: String, totalTurns: Int, profiles: List<PlayerProfile>) {
        viewModelScope.launch {
            repository.saveGameResult(winnerId, winnerName, totalTurns, profiles)
        }
    }

    fun loadLeaderboard(onLoaded: (List<PlayerProfile>) -> Unit) {
        viewModelScope.launch {
            val list = repository.loadLeaderboard()
            onLoaded(list)
        }
    }

    fun resetToLobby() {
        onResetToLobby?.invoke()
    }

    private fun checkTurnAndHand(state: GameState, player: Game.InGamePlayer, card: Card): String? {
        if (state.players.indexOf(player) != state.currentPlayerIndex) {
            val currentName = state.players.getOrNull(state.currentPlayerIndex)?.name ?: "другого игрока"
            return "Сейчас ход игрока $currentName!"
        }
        val hasCard = player.hand.any { it::class == card::class && it.color == card.color &&
                (it !is NumberCard || (card is NumberCard && it.value == card.value)) }
        if (!hasCard) return "У вас нет этой карты!"
        if (!card.canPlayOn(state.topCard, player, state)) {
            return "Эту карту нельзя выложить на ${getCardName(state.topCard)}!"
        }
        return null
    }

    private fun checkForgottenSwintus(playerId: UUID, handSizeBefore: Int, shoutedSwintus: Boolean, turnNumber: Int) {
        if (handSizeBefore == 2 && !shoutedSwintus) {
            forgottenSwintusTurns[playerId] = turnNumber
        }
    }

    private fun getCurrentPlayerSafely(state: GameState): Game.InGamePlayer {
        val size = state.players.size
        val safeIndex = ((state.currentPlayerIndex % size) + size) % size
        return state.players[safeIndex]
    }

    private fun checkAccuseValidity(targetPlayer: Game.InGamePlayer, violationTurn: Int?, currentTurn: Int, playersCount: Int): String? {
        if (targetPlayer.hand.size != 1) return "У этого игрока не одна карта!"
        if (violationTurn == null) return "Этот игрок не нарушал правила!"
        if (currentTurn - violationTurn > playersCount) {
            forgottenSwintusTurns.remove(targetPlayer.playerId)
            return "Время обвинения истекло! Прошел целый круг."
        }
        return null
    }

    private fun executePenaltyDrawn(state: GameState, targetPlayer: Game.InGamePlayer) {
        val updatedHand = targetPlayer.hand.toMutableList()
        repeat(2) {
            if (state.giveCard.cards.isEmpty() && state.discardCard.cards.isNotEmpty()) {
                recycleDiscardPile(state)
            }
            if (state.giveCard.cards.isNotEmpty()) {
                updatedHand.add(state.giveCard.draw(state.discardCard))
            }
        }
        targetPlayer.hand = updatedHand
    }

    private fun recycleDiscardPile(state: GameState) {
        val recycled = mutableListOf<Card>()
        while (state.discardCard.cards.isNotEmpty()) {
            recycled.add(state.discardCard.cards.pop())
        }
        recycled.shuffle()
        state.giveCard.cards.addAll(recycled)
    }

    private fun updateVersion() {
        stateVersion.value += 1
    }

    fun getCardName(card: Card): String {
        return when (card) {
            is NumberCard -> "${formatColor(card.color)} Цифра ${card.value}"
            is ActionCard -> "${formatColor(card.color)} [${card.effect::class.java.simpleName.replace("Effect", "").uppercase()}]"
            is WildCard -> "ПОЛИСВИН ${formatWildChosenColor(card.chosenColor)}".trim()
            else -> card.javaClass.simpleName.replace("Card", "")
        }
    }

    private fun formatColor(color: Game.Color): String = when (color) {
        Game.Color.RED -> "Красный"
        Game.Color.GREEN -> "Зеленый"
        Game.Color.BLUE -> "Синий"
        Game.Color.YELLOW -> "Желтый"
        Game.Color.GRAY -> "Серый"
    }

    private fun formatWildChosenColor(chosenColor: Game.Color): String {
        if (chosenColor == Game.Color.GRAY) return ""
        return "(Цвет: ${formatColor(chosenColor)})"
    }

    fun Card.canPlayOn(topCard: Card, player: Game.InGamePlayer, state: GameState): Boolean {
        if (this is WildCard || this.color == Game.Color.GRAY) return true
        if (topCard is WildCard) return this.color == topCard.chosenColor
        if (this.color == topCard.color) return true
        if (this is NumberCard && topCard is NumberCard) return this.value == topCard.value
        if (this is ActionCard && topCard is ActionCard) return this.effect::class == topCard.effect::class
        return false
    }

    suspend fun loadLeaderboard(): List<PlayerProfile> {
        return repository.loadLeaderboard()
    }
}