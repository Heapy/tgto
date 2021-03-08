package io.heapy.tgto.dao

import jetbrains.exodus.database.TransientEntityStore
import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.XdEntity
import kotlinx.dnq.XdNaturalEntityType
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.single
import kotlinx.dnq.query.singleOrNull
import kotlinx.dnq.query.sortedBy
import kotlinx.dnq.query.take
import kotlinx.dnq.query.toList
import kotlinx.dnq.xdParent
import kotlinx.dnq.xdRequiredDateTimeProp
import kotlinx.dnq.xdRequiredStringProp
import org.joda.time.DateTime
import java.util.Date

data class MessageDTO(
    val id: String,
    val text: String,
    val created: Date,
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

class XdMessage(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdMessage>()
    var created by xdRequiredDateTimeProp()
    var id by xdRequiredStringProp()
    var text by xdRequiredStringProp()
    var user: XdUser by xdParent(XdUser::messages)
}

class XdMessageDao(
    private val entityStore: TransientEntityStore,
) : MessageDao {
    override suspend fun insert(message: MessageDTO) {
        entityStore.transactional {
            val user = XdUser.filter { it.tgId.eq(message.tgId) }.single()
            XdMessage.new {
                this.text = message.text
                this.created = DateTime(message.created.time)
                this.id = message.id
                this.user = user
            }
        }
    }

    override suspend fun getById(url: String, id: String): MessageDTO? {
        return entityStore.transactional(readonly = true) {
            XdUser.filter { it.url.eq(url) }
                .singleOrNull()
                ?.messages
                ?.filter { it.id.eq(id) }
                ?.singleOrNull()
                ?.toDto()
        }
    }

    override suspend fun list(tgId: String, limit: Int): List<MessageDTO> {
        return entityStore.transactional(readonly = true) {
            XdUser.filter { it.tgId.eq(tgId) }
                .single()
                .messages
                .sortedBy(XdMessage::created, asc = false)
                .take(limit)
                .toList()
                .map { it.toDto() }
        }
    }

    private fun XdMessage.toDto(): MessageDTO {
        return MessageDTO(
            id = id,
            text = text,
            created = created.toDate(),
            tgId = user.tgId,
        )
    }
}
