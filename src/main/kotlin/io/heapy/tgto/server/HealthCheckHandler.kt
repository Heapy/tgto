package io.heapy.tgto.server

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.StatusCodes

class HealthCheckHandler : HttpHandler {
    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.statusCode = StatusCodes.OK
        exchange.responseSender.send("""{"status":"ok"}""")
    }
}
