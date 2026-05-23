package view

import data.local.db.SwintusDatabase
import data.repository.GameRepository

object DatabaseProvider {
    lateinit var database: SwintusDatabase
    lateinit var repository: GameRepository
}