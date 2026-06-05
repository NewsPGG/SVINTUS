package data.local.db

import data.local.dao.SwintusDao

class SwintusDatabase {
    fun swintusDao(): SwintusDao = SwintusDao()
}