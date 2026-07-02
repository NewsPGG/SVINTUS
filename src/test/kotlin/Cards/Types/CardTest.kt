package Cards.Types

import Game.Color
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CardTest {
    @Test
    fun `NumberCard should correctly initialize properties`() {
        val card = NumberCard(Color.RED, 5)
        assertEquals(Color.RED, card.color)
        assertEquals(5, card.value)
    }

    @Test
    fun `WildCard chosenColor should be changeable`() {
        val wild = WildCard(Color.GRAY, Color.GRAY)
        wild.chosenColor = Color.YELLOW
        assertEquals(Color.YELLOW, wild.chosenColor)
    }
}