package io.heapy.tgto

import io.heapy.tgto.commands.*
import io.heapy.tgto.dao.*
import io.heapy.tgto.server.UndertowFeedServer
import io.heapy.tgto.services.CommonMarkMarkdownService
import io.heapy.tgto.services.MarkdownService
import io.heapy.tgto.services.RomeFeedBuilder
import org.mapdb.DB
import org.mapdb.DBMaker.fileDB
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import kotlin.concurrent.thread

open class ApplicationFactory {
    open val appConfiguration: AppConfiguration by lazy {
        AppConfiguration()
    }

    open val messageDao: MessageDao by lazy {
        MapDbMessageDao(db, userDao as MapDbUserDao)
    }

    open val userDao: UserDao by lazy {
        MapDbUserDao(db)
    }

    open val db: DB by lazy {
        fileDB("${appConfiguration.storePath}/tgto.db")
            .transactionEnable()
            .make()
    }

    open val userInfo: UserInfo by lazy {
        DefaultUserInfo(appConfiguration)
    }

    open val uniquePathGenerator: UniquePathGenerator by lazy {
        UuidUniquePathGenerator()
    }

    open val markdownService: MarkdownService by lazy {
        CommonMarkMarkdownService()
    }

    open val feedBuilder: RomeFeedBuilder by lazy {
        RomeFeedBuilder(
            messageDao = messageDao,
            userInfo = userInfo,
            configuration = appConfiguration,
            markdownService = markdownService
        )
    }

    open val server: UndertowFeedServer by lazy {
        UndertowFeedServer(userDao, feedBuilder, messageDao, markdownService)
    }

    open val commandExecutor: CommandExecutor by lazy {
        DefaultCommandExecutor(
            commands = listOf(
                MyUrlCommand(userDao, userInfo),
                NewUrlCommand(userDao, uniquePathGenerator, userInfo),
                StartCommand(userDao, uniquePathGenerator, userInfo),
                PingPongCommand()
            ),
            fallbackCommand = SaveCommand(messageDao)
        )
    }

    open val tgBot: TgtoBot by lazy {
        TgtoBot(
            appConfiguration = appConfiguration,
            commandExecutor = commandExecutor,
        )
    }

    open fun start() {
        try {
            migrate(userDao, messageDao)

            server.run()

            val botSession = TelegramBotsApi(DefaultBotSession::class.java)
                .registerBot(tgBot)

            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                botSession.stop()
                db.close()
            })

            LOGGER.info("Bot started.")
        } catch (e: Exception) {
            LOGGER.error("Error in bot.", e)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ApplicationFactory::class.java)
    }
}
