package io.heapy.tgto.dao

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mapdb.DB
import org.mapdb.DBMaker
import java.nio.file.Files
import kotlin.properties.Delegates.notNull

class MapDbUserDaoTest {

    private var db: DB by notNull()

    @BeforeEach
    fun before() {
        val path = Files.createTempDirectory("tgto").toAbsolutePath().resolve("tgto.db").toString()
        db = DBMaker.fileDB(path).make()
    }

    @AfterEach
    fun after() {
        db.close()
    }

    @Test
    fun create() = runBlocking<Unit> {
        val mapDbUserDao = MapDbUserDao(db)
        mapDbUserDao.create(UserDTO("1", "3"))

        assertEquals(1, mapDbUserDao.tgIdToUrl.size)
        assertEquals(1, mapDbUserDao.urlToTgId.size)
        assertEquals("1", mapDbUserDao.tgIdToUrl["3"])
        assertEquals("3", mapDbUserDao.urlToTgId["1"])
    }

    @Test
    fun update() = runBlocking<Unit> {
        val mapDbUserDao = MapDbUserDao(db)
        mapDbUserDao.create(UserDTO("1", "1"))
        mapDbUserDao.update(UserDTO("2", "1"))

        assertEquals(1, mapDbUserDao.tgIdToUrl.size)
        assertEquals(1, mapDbUserDao.urlToTgId.size)
        assertEquals("2", mapDbUserDao.tgIdToUrl["1"])
        assertEquals("1", mapDbUserDao.urlToTgId["2"])
    }

    @Test
    fun findByUserId() = runBlocking<Unit> {
        val mapDbUserDao = MapDbUserDao(db)
        mapDbUserDao.create(UserDTO("2", "1"))

        assertEquals(UserDTO("2", "1"), mapDbUserDao.findByUserId("1"))
    }

    @Test
    fun findByUrl() = runBlocking<Unit> {
        val mapDbUserDao = MapDbUserDao(db)
        mapDbUserDao.create(UserDTO("2", "1"))

        assertEquals(UserDTO("2", "1"), mapDbUserDao.findByUrl("2"))
    }
}
