package Game

import Cards.Types.NumberCard
import Application.PlayerProfile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.UUID

class InGamePlayerTest {
    @Test
    fun `canPlayCard should return true when colors match`() {
        val player = InGamePlayer(PlayerProfile(UUID.randomUUID(), "T", 0,0,0))
        player.hand = mutableListOf(NumberCard(Color.RED, 5))
        val topCard = NumberCard(Color.RED, 9)
        assertTrue(player.canPlayCard(topCard))
    }

    @Test
    fun `canPlayCard should return true when values match`() {
        val player = InGamePlayer(PlayerProfile(UUID.randomUUID(), "T", 0,0,0))
        player.hand = mutableListOf(NumberCard(Color.BLUE, 7))
        val topCard = NumberCard(Color.YELLOW, 7)
        assertTrue(player.canPlayCard(topCard))
    }

    @Test
    fun `drawCards should remove cards from deck and add to hand`() {
        val player = InGamePlayer(PlayerProfile(UUID.randomUUID(), "T", 0,0,0))
        val deck = GiveCard()
        repeat(10) { deck.cards.push(NumberCard(Color.GREEN, 1)) }

        player.drawCards(3, deck)
        assertEquals(3, player.hand.size)
        assertEquals(7, deck.cards.size)
    }
}