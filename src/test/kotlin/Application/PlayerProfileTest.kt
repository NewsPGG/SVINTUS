package Application

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.UUID

class PlayerProfileTest {

    @Test
    fun `PlayerProfile should correctly store and update player data`() {
        val testId = UUID.randomUUID()
        val testName = "SwintusMaster"

        val profile = PlayerProfile(
            id = testId,
            username = testName,
            rating = 1200,
            gamesPlayed = 10,
            wins = 5
        )

        assertEquals(testId, profile.id)
        assertEquals(testName, profile.username)
        assertEquals(1200, profile.rating)
        assertEquals(10, profile.gamesPlayed)
        assertEquals(5, profile.wins)
    }

    @Test
    fun `PlayerProfile should allow updating mutable fields`() {
        val profile = PlayerProfile(UUID.randomUUID(), "Player", 0, 0, 0)

        profile.rating += 50
        profile.gamesPlayed += 1
        profile.wins += 1

        assertEquals(50, profile.rating)
        assertEquals(1, profile.gamesPlayed)
        assertEquals(1, profile.wins)
    }
}