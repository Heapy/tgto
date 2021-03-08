@file:JvmName("Application")
package io.heapy.tgto

import io.heapy.tgto.commands.MyUrlCommand
import io.heapy.tgto.commands.NewUrlCommand
import io.heapy.tgto.commands.PingPongCommand
import io.heapy.tgto.commands.SaveCommand
import io.heapy.tgto.commands.StartCommand
import io.heapy.tgto.dao.XdMessage
import io.heapy.tgto.dao.XdMessageDao
import io.heapy.tgto.dao.XdUser
import io.heapy.tgto.dao.XdUserDao
import io.heapy.tgto.server.UndertowFeedServer
import io.heapy.tgto.services.CommonMarkMarkdownService
import io.heapy.tgto.services.RomeFeedBuilder
import kotlinx.dnq.XdModel
import kotlinx.dnq.store.container.StaticStoreContainer
import kotlinx.dnq.util.initMetaData
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.io.File
import kotlin.concurrent.thread

/**
 * Implementation that uses env as default variables
 *
 * @author Ruslan Ibragimov
 */
data class AppConfiguration(
    val storePath: String = System.getenv("TGTO_STORE_PATH") ?: "./.xodus-dnq-data",
    val token: String = System.getenv("TGTO_BOT_TOKEN") ?: throw RuntimeException("Bot token required"),
    val baseUrl: String = System.getenv("TGTO_BASE_URL") ?: "http://localhost:8080/",
)

private val LOGGER = LoggerFactory.getLogger(AppConfiguration::class.java)

fun main() {
    try {
        val appConfiguration = AppConfiguration()

        XdModel.registerNodes(XdMessage, XdUser)
        val xodusStore = StaticStoreContainer.init(
            dbFolder = File(appConfiguration.storePath),
            environmentName = "db"
        )
        initMetaData(XdModel.hierarchy, xodusStore)

        val messageDao = XdMessageDao(xodusStore)
        val userDao = XdUserDao(xodusStore)

        val userInfo = DefaultUserInfo(appConfiguration)
        val uniquePathGenerator = UuidUniquePathGenerator()
        val markdownService = CommonMarkMarkdownService()

        val feedBuilder = RomeFeedBuilder(messageDao, userInfo, appConfiguration, markdownService)

        UndertowFeedServer(userDao, feedBuilder, messageDao, markdownService).run()

        val commandExecutor = DefaultCommandExecutor(
            commands = listOf(
                MyUrlCommand(userDao, userInfo),
                NewUrlCommand(userDao, uniquePathGenerator, userInfo),
                StartCommand(userDao, uniquePathGenerator, userInfo),
                PingPongCommand()
            ),
            fallbackCommand = SaveCommand(messageDao)
        )

        val tgBot = TgtoBot(
            appConfiguration = appConfiguration,
            commandExecutor = commandExecutor,
        )

        val botSession = TelegramBotsApi(DefaultBotSession::class.java)
            .registerBot(tgBot)

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            botSession.stop()
            xodusStore.close()
        })

        LOGGER.info("Bot started.")
    } catch (e: Exception) {
        LOGGER.error("Error in bot.", e)
    }
}
