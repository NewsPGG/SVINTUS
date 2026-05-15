package Integration

import Application.*
import Game.*
import Cards.Types.*
import Validator.TurnActions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.*

class GameIntegrationTests {
    private fun setupGame(playerNames: List<String>): GameAdministrator {
        val admin = GameAdministrator()
        val profiles = playerNames.map { PlayerProfile(UUID.randomUUID(), it, 0, 0, 0) }
        admin.startGame(profiles)
        return admin
    }

    @Test
    fun `Сценарий 1 - Успешный ход цифровой картой`() {
        val admin = setupGame(listOf("Игрок 1", "Игрок 2"))
        val state = admin.gameState
        val player = state.players[0]

        val validCard = NumberCard(state.topCard.color, 7)
        player.hand.add(validCard)

        val turn = PlayCardTurn(state.gameId, 1, player, validCard, validCard.color, false)
        val result = admin.processTurn(turn)

        assertNull(result)
        assertEquals(validCard, state.topCard)
        assertEquals(1, state.currentPlayerIndex)
    }

    @Test
    fun `Сценарий 2 - Блокировка хода неверной картой (Валидатор + Логика)`() {
        val admin = setupGame(listOf("Игрок 1"))
        val state = admin.gameState
        val player = state.players[0]

        val wrongColor = Color.values().find { it != state.topCard.color && it != Color.GRAY }!!
        val invalidCard = NumberCard(wrongColor, -1)
        player.hand.add(invalidCard)

        val turn = PlayCardTurn(state.gameId, 1, player, invalidCard, wrongColor, false)
        val result = admin.processTurn(turn)

        assertNotNull(result)
        assertNotEquals(invalidCard, state.topCard)
        assertEquals(0, state.currentPlayerIndex)
    }

    @Test
    fun `Сценарий 3 - Розыгрыш Полипца (WildCard) со сменой цвета`() {
        val admin = setupGame(listOf("Игрок 1"))
        val player = admin.gameState.players[0]
        val wild = WildCard(Color.GRAY, Color.GRAY)
        player.hand.add(wild)

        val turn = PlayCardTurn(admin.gameState.gameId, 1, player, wild, Color.BLUE, false)
        admin.processTurn(turn)

        val cardOnTable = admin.gameState.topCard as WildCard
        assertEquals(Color.BLUE, cardOnTable.chosenColor)
    }
}