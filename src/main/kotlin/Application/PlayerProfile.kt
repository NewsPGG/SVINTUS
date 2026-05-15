package Application

import java.util.UUID

data class PlayerProfile(
    val id: UUID,
    val username: String,
    var rating: Int,
    var gamesPlayed: Int,
    var wins: Int
)