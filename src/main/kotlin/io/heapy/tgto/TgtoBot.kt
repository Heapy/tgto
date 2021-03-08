package io.heapy.tgto

import io.heapy.tgto.commands.DeleteMessageAction
import io.heapy.tgto.commands.SendMessageAction
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * @author Ruslan Ibragimov
 */
class TgtoBot(
    private val appConfiguration: AppConfiguration,
    private val commandExecutor: CommandExecutor,
) : TelegramLongPollingBot() {
    override fun getBotToken() = appConfiguration.token
    override fun getBotUsername() = "ToRssBot"

    override fun onUpdateReceived(update: Update) {
        GlobalScope.launch(IO) {
            commandExecutor.onReceive(update).forEach { action ->
                when (action) {
                    is SendMessageAction -> execute(SendMessage(
                        action.chatId.toString(),
                        action.message
                    ))
                    is DeleteMessageAction -> execute(DeleteMessage(
                        action.chatId.toString(),
                        action.messageId
                    ))
                }
            }
        }
    }
}
