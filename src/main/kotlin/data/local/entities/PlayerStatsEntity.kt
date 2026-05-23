package data.local.entities

data class PlayerStatsEntity(
    val playerId: String,
    val username: String,
    val gamesPlayed: Int,
    val wins: Int,
    val rating: Int
)