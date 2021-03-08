package io.heapy.tgto.commands

import io.heapy.tgto.UserInfo
import io.heapy.tgto.dao.UserDao
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Handles /myurl command.
 *
 * @author Ruslan Ibragimov
 */
class MyUrlCommand(
    private val userDao: UserDao,
    private val userInfo: UserInfo
) : Command {
    override val name = "/myurl"

    override suspend fun handler(update: Update): List<TgAction> {
        val user = userDao.findByUserId(update.message.from.id.toString())
            ?: throw RuntimeException("Received command, but no user saved in database.")

        return listOf(
            SendMessageAction(
                chatId = update.message.chatId,
                message = userInfo.getFeedUrl(user)
            ),
            update.deleteAction()
        )
    }
}
