package io.heapy.tgto.server

import io.heapy.tgto.dao.MessageDao
import io.heapy.tgto.services.MarkdownService
import io.undertow.util.Headers

/**
 * Provides [CoroutinesHandler].
 *
 * @author Ruslan Ibragimov
 */
class FeedItemHandlerFactory(
    private val messageDao: MessageDao,
    private val markdownService: MarkdownService
) {
    fun handler() = CoroutinesHandler {
        val url = it.queryParameters["url"]?.poll()
            ?: throw NotFoundException("Please check your url.")
        val itemId = it.queryParameters["itemId"]?.poll()
            ?: throw NotFoundException("Please check your url.")
        val message = messageDao.getById(url, itemId)
            ?: throw NotFoundException("Message with this id not found.")

        it.responseHeaders.add(Headers.CONTENT_TYPE, "text/html")
        it.responseSender.send("""
            <html>
            <head>
            <meta charset="UTF-8">
              <title>Message: ${message.id}</title>
            </head>
            <body>
              ${markdownService.render(message.text)}
            </body>
            </html>
            """.trimIndent()
        )
    }
}
