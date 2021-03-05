package io.heapy.tgto

import io.heapy.tgto.commands.DeleteMessageAction
import io.heapy.tgto.commands.SendMessageAction
import io.heapy.tgto.configuration.AppConfiguration
import io.heapy.tgto.coroutines.coExecute
import io.heapy.tgto.coroutines.serverContext
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
    private val shutdownManager: ShutdownManager
) : TelegramLongPollingBot() {
    override fun getBotToken() = appConfiguration.token
    override fun getBotUsername() = "ToRssBot"

    override fun onUpdateReceived(update: Update) {
        if (shutdownManager.isShutdown) return

        GlobalScope.launch(serverContext) {
            commandExecutor.onReceive(update).forEach { action ->
                when (action) {
                    is SendMessageAction -> coExecute(SendMessage(
                        action.chatId.toString(),
                        action.message
                    ))
                    is DeleteMessageAction -> coExecute(DeleteMessage(
                        action.chatId.toString(),
                        action.messageId
                    ))
                }
            }
        }
    }
}
