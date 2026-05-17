package repository

import Application.PlayerProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

@Serializable
data class PlayerDbEntity(
    val id: String,
    val username: String,
    val rating: Int = 0,
    val gamesPlayed: Int = 0,
    val wins: Int = 0
)

class LocalPlayerDatabase(private val filePath: String = "local_swintus_db.json") {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val file = File(filePath)
    private val memoryTable = mutableMapOf<String, PlayerDbEntity>()

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        if (file.exists()) {
            try {
                val content = file.readText()
                val records = json.decodeFromString<List<PlayerDbEntity>>(content)
                records.forEach { memoryTable[it.id] = it }
            } catch (e: Exception) {
                System.err.println("Ошибка чтения локальной БД: ${e.message}")
            }
        }
    }

    private fun flushToDisk() {
        try {
            val content = json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(PlayerDbEntity.serializer()),
                memoryTable.values.toList()
            )
            file.writeText(content)
        } catch (e: Exception) {
            System.err.println("Ошибка записи базы данных на диск: ${e.message}")
        }
    }

    @Synchronized
    fun saveOrUpdateResult(username: String, isWinner: Boolean): PlayerProfile {
        val cleanName = username.trim()
        val existing = memoryTable.values.find { it.username.equals(cleanName, ignoreCase = true) }

        val updatedEntity = if (existing != null) {
            existing.copy(
                gamesPlayed = existing.gamesPlayed + 1,
                wins = if (isWinner) existing.wins + 1 else existing.wins,
                rating = if (isWinner) existing.rating + 10 else existing.rating - 3
            )
        } else {
            PlayerDbEntity(
                id = UUID.randomUUID().toString(),
                username = cleanName,
                rating = if (isWinner) 10 else 0,
                gamesPlayed = 1,
                wins = if (isWinner) 1 else 0
            )
        }

        memoryTable[updatedEntity.id] = updatedEntity
        flushToDisk()

        return PlayerProfile(
            UUID.fromString(updatedEntity.id),
            updatedEntity.username,
            updatedEntity.rating,
            updatedEntity.gamesPlayed,
            updatedEntity.wins
        )
    }

    @Synchronized
    fun getProfile(username: String): PlayerProfile? {
        val entity = memoryTable.values.find { it.username.equals(username, ignoreCase = true) } ?: return null

        return PlayerProfile(
            UUID.fromString(entity.id),
            entity.username,
            entity.rating,
            entity.gamesPlayed,
            entity.wins
        )
    }

    @Synchronized
    fun getAllStats(): List<PlayerDbEntity> = memoryTable.values.sortedByDescending { it.wins }
}