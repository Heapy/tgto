package io.heapy.tgto.services

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/**
 * Service that parses markdown string, and return html string.
 *
 * @author Ruslan Ibragimov
 */
interface MarkdownService {
    suspend fun render(md: String): String
}

class CommonMarkMarkdownService : MarkdownService {
    private val parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()

    override suspend fun render(md: String): String = withContext(IO) {
        val document = parser.parse(md)
        renderer.render(document)
    }
}
