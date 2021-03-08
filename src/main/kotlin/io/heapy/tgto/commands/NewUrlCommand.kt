package io.heapy.tgto.commands

import io.heapy.tgto.UniquePathGenerator
import io.heapy.tgto.UserInfo
import io.heapy.tgto.dao.UserDao
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Handles /newurl command.
 *
 * @author Ruslan Ibragimov
 */
class NewUrlCommand(
    private val userDao: UserDao,
    private val uniquePathGenerator: UniquePathGenerator,
    private val userInfo: UserInfo
) : Command {
    override val name = "/newurl"

    override suspend fun handler(update: Update): List<TgAction> {
        val user = userDao.findByUserId(update.message.from.id.toString())
            ?: throw RuntimeException("Received command, but no user saved in database.")

        val new = user.copy(url = uniquePathGenerator.get())
        userDao.update(new)

        return listOf(
            SendMessageAction(
                chatId = update.message.chatId,
                message = userInfo.getFeedUrl(new)
            ),
            update.deleteAction()
        )
    }
}
