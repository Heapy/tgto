package io.heapy.tgto.commands

import io.heapy.tgto.dao.MessageDao
import io.heapy.tgto.dao.MessageDTO
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.Date

/**
 * Command that saves message to database.
 *
 * @author Ruslan Ibragimov
 */
class SaveCommand(
    private val messageDao: MessageDao
) : Command {
    override val name = "/save"

    override suspend fun handler(update: Update): List<TgAction> {
        val message = update.message
        val tgId = message.from.id.toString()

        val messageDto = MessageDTO(
            id = message.messageId.toString(),
            text = message.text,
            created = message.date * 1000L,
            tgId = tgId,
        )

        messageDao.insert(messageDto)

        return listOf(
            SendMessageAction(
                chatId = message.chatId,
                message = """Message "${message.text}" saved."""
            ),
            update.deleteAction()
        )
    }
}
