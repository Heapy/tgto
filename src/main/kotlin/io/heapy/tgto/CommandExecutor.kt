package io.heapy.tgto

import io.heapy.tgto.commands.Command
import io.heapy.tgto.commands.TgAction
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Interface for receiving messages from user.
 *
 * @author Ruslan Ibragimov
 */
interface CommandExecutor {
    suspend fun onReceive(update: Update): List<TgAction>
}

class DefaultCommandExecutor(
    private val commands: List<Command>,
    private val fallbackCommand: Command
) : CommandExecutor {
    private val _commands = commands.associateBy { it.name }

    override suspend fun onReceive(update: Update): List<TgAction> {
        // Updates comes with null `message`, but field `editedMessage`.
        val message = update.message ?: return listOf()
        val userId = message.from.id.toLong()

        // Accept messages only from private chat with bot
        return if (message.chat.id == userId && message.text != null) {
            _commands.getOrDefault(message.text, fallbackCommand).handler(update)
        } else {
            listOf()
        }
    }
}
