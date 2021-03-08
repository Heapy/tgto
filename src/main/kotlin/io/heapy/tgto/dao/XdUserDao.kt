package io.heapy.tgto.dao

import jetbrains.exodus.database.TransientEntityStore
import jetbrains.exodus.entitystore.Entity
import kotlinx.dnq.XdEntity
import kotlinx.dnq.XdNaturalEntityType
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.single
import kotlinx.dnq.query.singleOrNull
import kotlinx.dnq.xdChildren0_N
import kotlinx.dnq.xdRequiredStringProp

data class UserDTO(
    val url: String,
    val tgId: String,
)

/**
 * @author Ruslan Ibragimov
 */
interface UserDao {
    suspend fun create(user: UserDTO)
    suspend fun update(user: UserDTO)
    suspend fun findByUserId(tgId: String): UserDTO?
    suspend fun findByUrl(url: String): UserDTO?
}

class XdUser(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdUser>()
    val messages by xdChildren0_N(XdMessage::user)
    var tgId by xdRequiredStringProp()
    var url by xdRequiredStringProp()
}

class XdUserDao(
    private val entityStore: TransientEntityStore,
) : UserDao {
    override suspend fun create(user: UserDTO) {
        entityStore.transactional {
            XdUser.new {
                this.tgId = user.tgId
                this.url = user.url
            }
        }
    }

    override suspend fun update(user: UserDTO) {
        entityStore.transactional {
            XdUser.filter { it.tgId.eq(user.tgId) }.single().also {
                it.url = user.url
            }
        }
    }

    override suspend fun findByUserId(tgId: String): UserDTO? {
        return entityStore.transactional(readonly = true) {
            XdUser.filter { it.tgId.eq(tgId) }.singleOrNull()?.toDto()
        }
    }

    override suspend fun findByUrl(url: String): UserDTO? {
        return entityStore.transactional(readonly = true) {
            XdUser.filter { it.url.eq(url) }.singleOrNull()?.toDto()
        }
    }

    private fun XdUser.toDto(): UserDTO {
        return UserDTO(
            url = url,
            tgId = tgId
        )
    }
}
