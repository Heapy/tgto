package io.heapy.tgto.dao

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.mapdb.DB
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import org.mapdb.serializer.SerializerArrayTuple
import java.util.*

@Serializable
data class MessageDTO(
    val id: String,
    val text: String,
    val created: Long,
    val tgId: String,
)

/**
 * @author Ruslan Ibragimov
 */
interface MessageDao {
    suspend fun insert(message: MessageDTO)
    suspend fun getById(url: String, id: String): MessageDTO?
    suspend fun list(tgId: String, limit: Int): List<MessageDTO>
}

class MapDbMessageDao(
    private val db: DB,
    private val mapDbUserDao: MapDbUserDao
) : MessageDao {
    internal val userIdToMessageId: NavigableSet<Array<Any>> = db
        .treeSet("userIdToMessageId")
        .serializer(SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .createOrOpen()
    internal val messageIdToMessage: HTreeMap<String, String> = db
        .hashMap("messageIdToMessage", Serializer.STRING, Serializer.STRING)
        .createOrOpen()

    override suspend fun insert(message: MessageDTO) {
        val data = Json.encodeToString(MessageDTO.serializer(), message)
        userIdToMessageId.add(arrayOf(message.tgId, message.id))
        messageIdToMessage[message.id] = data
        db.commit()
    }

    override suspend fun getById(url: String, id: String): MessageDTO? {
        return messageIdToMessage[id]?.let { data ->
            mapDbUserDao.findByUrl(url)?.let { userDTO ->
                val messageDto = Json.decodeFromString(MessageDTO.serializer(), data)
                if (userDTO.tgId == messageDto.tgId) messageDto else null
            }
        }
    }

    override suspend fun list(tgId: String, limit: Int): List<MessageDTO> {
        val set: SortedSet<Array<Any>> = userIdToMessageId.subSet(
            arrayOf(tgId),
            arrayOf(tgId, null) as Array<Any>
        )

        return set.asSequence()
            .take(limit)
            .map { it[1] as String }
            .map { messageId -> messageIdToMessage[messageId] }
            .filterNotNull()
            .map { message -> Json.decodeFromString(MessageDTO.serializer(), message) }
            .toList()
    }
}
