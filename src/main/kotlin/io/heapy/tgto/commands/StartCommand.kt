package io.heapy.tgto.commands

import io.heapy.tgto.ResponseChannel
import io.heapy.tgto.UniquePathGenerator
import io.heapy.tgto.UserInfo
import io.heapy.tgto.dao.CUserDao
import io.heapy.tgto.db.tables.pojos.TgUser
import io.heapy.integration.slf4j.logger
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Handles user join.
 *
 * @author Ruslan Ibragimov
 */
class StartCommand(
    private val userDao: CUserDao,
    private val responseChannel: ResponseChannel,
    private val pathGenerator: UniquePathGenerator,
    private val userInfo: UserInfo
) : Command {
    override val name = "/start"

    override suspend fun handler(update: Update) {
        LOGGER.info("""User "${update.message.from.userName}" join.""")

        val userId = update.message.from.id.toLong()

        val user = userDao.findByUserId(userId) ?: run {
            TgUser().also {
                it.url = pathGenerator.get()
                it.userId = userId
                userDao.create(it)
            }
        }

        val chatId = update.message.chatId
        responseChannel.send(
            chatId = chatId,
            message = "Hello! Here your rss feed. Just send me messages, and they'll appear in your personal feed."
        )
        responseChannel.send(
            chatId = chatId,
            message = userInfo.getFeedUrl(user)
        )
    }

    private val LOGGER = logger<StartCommand>()
}
