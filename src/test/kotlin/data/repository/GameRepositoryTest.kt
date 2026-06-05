package data.repository

import Application.PlayerProfile
import data.local.dao.SwintusDao
import data.local.entities.PlayerStatsEntity
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.any
import java.util.UUID

class GameRepositoryTest {

    private val dao = mock(SwintusDao::class.java)
    private val repository = GameRepository(dao)

    @Test
    fun `saveGameResult updates win count and adds 5 points for winner`() = runTest {
        val winnerId = UUID.randomUUID()
        val winnerProfile = PlayerProfile(winnerId, "Кирилл", rating = 10, gamesPlayed = 2, wins = 1)

        val existingEntity = PlayerStatsEntity(winnerId.toString(), "Кирилл", gamesPlayed = 2, wins = 1, rating = 10)
        `when`(dao.getPlayerStats(winnerId.toString())).thenReturn(existingEntity)

        repository.saveGameResult(winnerId, "Кирилл", 15, listOf(winnerProfile))

        verify(dao).insertOrUpdatePlayer(
            PlayerStatsEntity(
                playerId = winnerId.toString(),
                username = "Кирилл",
                gamesPlayed = 3,
                wins = 2,
                rating = 15
            )
        )
    }

    @Test
    fun `saveGameResult deducts 2 points for loser`() = runTest {
        val loserId = UUID.randomUUID()
        val loserProfile = PlayerProfile(loserId, "Оппонент", rating = 10, gamesPlayed = 5, wins = 2)

        val existingEntity = PlayerStatsEntity(loserId.toString(), "Оппонент", gamesPlayed = 5, wins = 2, rating = 10)
        `when`(dao.getPlayerStats(loserId.toString())).thenReturn(existingEntity)

        // Передаем другой UUID в качестве победителя, чтобы этот игрок считался проигравшим
        repository.saveGameResult(UUID.randomUUID(), "Победитель", 15, listOf(loserProfile))

        verify(dao).insertOrUpdatePlayer(
            PlayerStatsEntity(
                playerId = loserId.toString(),
                username = "Оппонент",
                gamesPlayed = 6,
                wins = 2,
                rating = 8
            )
        )
    }

    @Test
    fun `saveGameResult keeps rating at zero if loser has low points`() = runTest {
        val loserId = UUID.randomUUID()
        val loserProfile = PlayerProfile(loserId, "Новичок", rating = 1, gamesPlayed = 0, wins = 0)

        val existingEntity = PlayerStatsEntity(loserId.toString(), "Новичок", gamesPlayed = 0, wins = 0, rating = 1)
        `when`(dao.getPlayerStats(loserId.toString())).thenReturn(existingEntity)

        repository.saveGameResult(UUID.randomUUID(), "Победитель", 15, listOf(loserProfile))

        verify(dao).insertOrUpdatePlayer(
            PlayerStatsEntity(
                playerId = loserId.toString(),
                username = "Новичок",
                gamesPlayed = 1,
                wins = 0,
                rating = 0 // Защита сработала: 1 - 2 = -1 -> округляется до 0
            )
        )
    }
}