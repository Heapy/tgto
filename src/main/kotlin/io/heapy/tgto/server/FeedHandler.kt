package io.heapy.tgto.server

import io.heapy.tgto.dao.UserDao
import io.heapy.tgto.services.FeedBuilder
import io.undertow.util.Headers

class DefaultFeedHandlerFactory(
    private val userDao: UserDao,
    private val feedBuilder: FeedBuilder
) {
    fun handler() = CoroutinesHandler {
        val url = it.queryParameters["url"]?.poll()
            ?: throw NotFoundException("Please check your url.")
        val user = userDao.findByUrl(url)
            ?: throw NotFoundException("Feed with this url not found.")

        it.responseHeaders.add(Headers.CONTENT_TYPE, "application/atom+xml")
        it.responseSender.send(feedBuilder.feed(user, 100))
    }
}
