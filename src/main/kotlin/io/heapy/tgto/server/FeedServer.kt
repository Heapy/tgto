package io.heapy.tgto.server

import io.heapy.tgto.dao.MessageDao
import io.heapy.tgto.dao.UserDao
import io.heapy.tgto.services.FeedBuilder
import io.heapy.tgto.services.MarkdownService
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.util.SameThreadExecutor
import io.undertow.util.StatusCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.concurrent.thread

/**
 * Server to host user feeds.
 *
 * @author Ruslan Ibragimov
 */
interface FeedServer {
    fun run()
}

class UndertowFeedServer(
    private val userDao: UserDao,
    private val feedBuilder: FeedBuilder,
    private val messageDao: MessageDao,
    private val markdownService: MarkdownService
) : FeedServer {
    override fun run() {
        val feedHandler = DefaultFeedHandlerFactory(userDao, feedBuilder).handler()
        val feedItemHandler = FeedItemHandlerFactory(messageDao, markdownService).handler()

        val routingHandler = RoutingHandler().also {
            it.get("/rss/{url}", feedHandler)
            it.get("/rss/{url}/{itemId}", feedItemHandler)
        }

        val rootHandler = ResourceHandler(
            ClassPathResourceManager(this::class.java.classLoader, "public"),
            routingHandler
        ).setCacheTime(YEAR_IN_SECONDS)

        val undertow = Undertow.builder()
            .addHttpListener(8080, "0.0.0.0", rootHandler)
            .setWorkerThreads(1) // All actual work happens in IO or on coroutines dispatcher
            .build()

        undertow.start()

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            undertow.stop()
        })
    }

    companion object {
        private const val YEAR_IN_SECONDS: Int = 60 * 60 * 24 * 365
    }
}

class NotFoundException(message: String) : RuntimeException(message)

/**
 * Bridge between thread and coroutines worlds.
 *
 * @author Ruslan Ibragimov
 */
class CoroutinesHandler(
    private val handler: suspend (HttpServerExchange) -> Unit
) : HttpHandler {
    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.dispatch(SameThreadExecutor.INSTANCE, Runnable {
            GlobalScope.launch(Dispatchers.Unconfined) {
                try {
                    handler(exchange)
                } catch (e: NotFoundException) {
                    exchange.statusCode = StatusCodes.NOT_FOUND
                    exchange.responseSender.send(e.message)
                } catch (e: Exception) {
                    exchange.statusCode = StatusCodes.INTERNAL_SERVER_ERROR
                    exchange.responseSender.send(e.message)
                }
            }
        })
    }
}
