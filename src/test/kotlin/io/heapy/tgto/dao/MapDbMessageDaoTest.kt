package io.heapy.tgto.dao

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mapdb.DB
import org.mapdb.DBMaker
import java.nio.file.Files
import kotlin.properties.Delegates

class MapDbMessageDaoTest {

    private var db: DB by Delegates.notNull()

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
    fun insert() = runBlocking<Unit> {
        val mapDbUserDao = MapDbUserDao(db)
        val mapDbMessageDao = MapDbMessageDao(db, mapDbUserDao)
        mapDbUserDao.create(UserDTO("1", "3"))
        val message = MessageDTO(
            "1",
            "Hello",
            0,
            "3"
        )
        mapDbMessageDao.insert(message)

        assertEquals(1, mapDbMessageDao.messageIdToMessage.size)
        assertEquals(
            Json.encodeToString(MessageDTO.serializer(), message),
            mapDbMessageDao.messageIdToMessage["1"]
        )
    }

    @Test
    fun getById() = runBlocking<Unit> {
        val mapDbUserDao = MapDbUserDao(db)
        val mapDbMessageDao = MapDbMessageDao(db, mapDbUserDao)
        mapDbUserDao.create(UserDTO("1", "3"))
        val message = MessageDTO(
            "1",
            "Hello",
            0,
            "3"
        )
        mapDbMessageDao.insert(message)

        assertEquals(message, mapDbMessageDao.getById("1", "1"))
    }

    @Test
    fun list() = runBlocking<Unit> {
        val mapDbUserDao = MapDbUserDao(db)
        val mapDbMessageDao = MapDbMessageDao(db, mapDbUserDao)
        mapDbUserDao.create(UserDTO("1", "3"))
        val message = MessageDTO(
            "1",
            "Hello",
            0,
            "3"
        )
        mapDbMessageDao.insert(message)

        assertIterableEquals(
            listOf(message),
            mapDbMessageDao.list("3", 100)
        )
    }
}
