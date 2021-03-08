package io.heapy.tgto.commands

import io.heapy.tgto.UniquePathGenerator
import io.heapy.tgto.UserInfo
import io.heapy.tgto.dao.UserDao
import io.heapy.tgto.dao.UserDTO
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Handles user join.
 *
 * @author Ruslan Ibragimov
 */
class StartCommand(
    private val userDao: UserDao,
    private val pathGenerator: UniquePathGenerator,
    private val userInfo: UserInfo
) : Command {
    override val name = "/start"

    override suspend fun handler(update: Update): List<TgAction> {
        LOGGER.info("""User "${update.message.from.userName}" join.""")

        val tgId = update.message.from.id.toString()

        val user = userDao.findByUserId(tgId) ?: run {
            UserDTO(
                url = pathGenerator.get(),
                tgId = tgId,
            ).also {
                userDao.create(it)
            }
        }

        return listOf(
            SendMessageAction(
                chatId = update.message.chatId,
                message = "Hello! Here your rss feed. Just send me messages, and they'll appear in your personal feed."
            ),
            SendMessageAction(
                chatId = update.message.chatId,
                message = userInfo.getFeedUrl(user)
            )
        )
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StartCommand::class.java)
    }
}
