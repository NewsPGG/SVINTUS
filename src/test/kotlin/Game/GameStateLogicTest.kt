package Game

import Cards.Types.NumberCard
import Application.PlayerProfile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.UUID

class GameStateLogicTest {
    @Test
    fun `getCurrentPlayer should return player corresponding to index`() {
        val p0 = InGamePlayer(PlayerProfile(UUID.randomUUID(), "P0", 0,0,0))
        val p1 = InGamePlayer(PlayerProfile(UUID.randomUUID(), "P1", 0,0,0))
        val state = GameState(UUID.randomUUID(), mutableListOf(p0, p1), GiveCard(), DiscardCard(), 1, 1, true, NumberCard(Color.RED, 0))
        val logic = GameStateLogic(state)

        assertEquals("P1", logic.getCurrentPlayer(state).name)
    }
}