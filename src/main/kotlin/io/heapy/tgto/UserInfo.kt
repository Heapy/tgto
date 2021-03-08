package io.heapy.tgto

import io.heapy.tgto.dao.MessageDTO
import io.heapy.tgto.dao.UserDTO

/**
 * Provides info like feed url for user.
 *
 * @author Ruslan Ibragimov
 */
interface UserInfo {
    fun getFeedUrl(user: UserDTO): String
    fun getFeedItemUrl(user: UserDTO, message: MessageDTO): String
}

class DefaultUserInfo(
    private val appConfiguration: AppConfiguration
) : UserInfo {
    override fun getFeedUrl(user: UserDTO): String {
        return "${appConfiguration.baseUrl}rss/${user.url}"
    }

    override fun getFeedItemUrl(user: UserDTO, message: MessageDTO): String {
        return "${appConfiguration.baseUrl}rss/${user.url}/${message.id}"
    }
}
