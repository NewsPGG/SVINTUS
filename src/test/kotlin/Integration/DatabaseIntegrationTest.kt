package integration

import Application.PlayerProfile
import androidx.room.Room
import data.local.db.SwintusDatabase
import data.local.dao.SwintusDao
import data.repository.GameRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.UUID

class DatabaseIntegrationTest {

    private lateinit var db: SwintusDatabase
    private lateinit var dao: SwintusDao
    private lateinit var repository: GameRepository

    @BeforeEach
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            androidx.room.Room::class.java, // Используется в KMP/JVM контексте
            SwintusDatabase::class.java
        ).build()
        dao = db.swintusDao()
        repository = GameRepository(dao)
    }

    @AfterEach
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `saveGameResult correctly writes to database tables and links data`() = runTest {
        val p1Id = UUID.randomUUID()
        val p2Id = UUID.randomUUID()

        val players = listOf(
            PlayerProfile(id = p1Id, username = "Игрок 1", rating = 10, gamesPlayed = 5, wins = 2),
            PlayerProfile(id = p2Id, username = "Игрок 2", rating = 4, gamesPlayed = 3, wins = 1)
        )

        repository.saveGameResult(
            winnerId = p1Id,
            winnerName = "Игрок 1",
            totalTurns = 24,
            players = players
        )

        val p1Stats = dao.getPlayerStats(p1Id.toString())
        assertNotNull(p1Stats)
        assertEquals("Игрок 1", p1Stats!!.username)
        assertEquals(6, p1Stats.gamesPlayed)
        assertEquals(3, p1Stats.wins)
        assertEquals(15, p1Stats.rating)

        val p2Stats = dao.getPlayerStats(p2Id.toString())
        assertNotNull(p2Stats)
        assertEquals("Игрок 2", p2Stats!!.username)
        assertEquals(4, p2Stats.gamesPlayed)
        assertEquals(1, p2Stats.wins)
        assertEquals(2, p2Stats.rating)

        val historyList = dao.getGameHistory()
        assertEquals(1, historyList.size)

        val gameRecord = historyList[0]
        assertEquals(p1Id.toString(), gameRecord.winnerId)
        assertEquals("Игрок 1", gameRecord.winnerName)
        assertEquals(24, gameRecord.totalTurns)
        assertEquals("$p1Id,$p2Id", gameRecord.playersList)
    }
}