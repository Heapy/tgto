package app.hea.tgto.commands

import app.hea.tgto.ResponseChannel
import app.hea.tgto.UniquePathGenerator
import app.hea.tgto.UserInfo
import app.hea.tgto.dao.CUserDao
import app.hea.tgto.logging.logger
import app.heap.tgto.db.tables.pojos.TgUser
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

        val user = TgUser().also {
            it.url = pathGenerator.get()
            it.userId = update.message.from.id.toLong()
        }
        userDao.create(user)

        val chatId = update.message.chatId
        responseChannel.send(
            chatId = chatId,
            message = "Hello! Here your rss feed. Just executeAsync me messages, and they'll appear in your feed."
        )
        responseChannel.send(
            chatId = chatId,
            message = userInfo.getFeedUrl(user)
        )
    }

    private val LOGGER = logger<StartCommand>()
}
