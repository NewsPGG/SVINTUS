package System

import Application.*
import Game.*
import Cards.Types.*
import Validator.TurnActions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.*

class SwintusSystemTests {

    @Test
    fun `System - Complete path to victory`() {
        val admin = GameAdministrator()
        val playerId = UUID.randomUUID()
        admin.startGame(listOf(PlayerProfile(playerId, "Winner", 0, 0, 0)))
        val player = admin.gameState.players[0]

        player.hand.clear()
        val lastCard = NumberCard(admin.gameState.topCard.color, 1)
        player.hand.add(lastCard)

        val turn = PlayCardTurn(admin.gameState.gameId, 1, player, lastCard, lastCard.color, true)
        admin.processTurn(turn)

        assertTrue(player.hand.isEmpty())
        assertFalse(admin.gameState.gameStatus)
    }

    @Test
    fun `System - Multiplayer turn sequence integrity`() {
        val admin = GameAdministrator()
        val profiles = listOf(
            PlayerProfile(UUID.randomUUID(), "A", 0, 0, 0),
            PlayerProfile(UUID.randomUUID(), "B", 0, 0, 0)
        )
        admin.startGame(profiles)

        val playerA = admin.gameState.players[0]

        admin.processTurn(DrawCardTurn(admin.gameState.gameId, 1, playerA, 1, false))
        assertEquals(0, admin.gameState.currentPlayerIndex)

        val card = NumberCard(admin.gameState.topCard.color, 9)
        playerA.hand.add(card)
        admin.processTurn(PlayCardTurn(admin.gameState.gameId, 1, playerA, card, card.color, false))

        assertEquals(1, admin.gameState.currentPlayerIndex)
    }
}