package io.heapy.tgto.dao

import org.mapdb.DB
import org.mapdb.HTreeMap
import org.mapdb.Serializer

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

class MapDbUserDao(
    private val db: DB
) : UserDao {
    internal val tgIdToUrl: HTreeMap<String, String> = db
        .hashMap("tgIdToUrl", Serializer.STRING, Serializer.STRING)
        .createOrOpen()
    internal val urlToTgId: HTreeMap<String, String> = db
        .hashMap("urlToTgId", Serializer.STRING, Serializer.STRING)
        .createOrOpen()

    override suspend fun create(user: UserDTO) {
        tgIdToUrl[user.tgId] = user.url
        urlToTgId[user.url] = user.tgId
        db.commit()
    }

    override suspend fun update(user: UserDTO) {
        val prevUrl = tgIdToUrl[user.tgId]
        tgIdToUrl[user.tgId] = user.url
        urlToTgId.remove(prevUrl, user.tgId)
        urlToTgId[user.url] = user.tgId
        db.commit()
    }

    override suspend fun findByUserId(tgId: String): UserDTO? {
        return tgIdToUrl[tgId]?.let {
            UserDTO(
                url = it,
                tgId = tgId
            )
        }
    }

    override suspend fun findByUrl(url: String): UserDTO? {
        return urlToTgId[url]?.let {
            UserDTO(
                url = url,
                tgId = it
            )
        }
    }
}
