@file:JvmName("Application")

package io.heapy.tgto

/**
 * Implementation that uses env as default variables
 *
 * @author Ruslan Ibragimov
 */
data class AppConfiguration(
    val storePath: String = System.getenv("TGTO_STORE_PATH") ?: "./.mapdb",
    val token: String = System.getenv("TGTO_BOT_TOKEN") ?: throw RuntimeException("Bot token required"),
    val baseUrl: String = System.getenv("TGTO_BASE_URL") ?: "http://localhost:8080/",
)

fun main() {
    ApplicationFactory().start()
}
