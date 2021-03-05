package io.heapy.tgto.configuration

import io.heapy.komodo.config.dotenv.Dotenv
import java.nio.file.Paths

private val env = Dotenv(Paths.get("./devops/.env"))

/**
 * Application Configuration
 *
 * @author Ruslan Ibragimov
 */
interface AppConfiguration {
    val token: String
    val baseUrl: String
    val ds: DataSourceConfiguration
}

/**
 * Implementation that uses env as default variables
 *
 * @author Ruslan Ibragimov
 */
class DefaultAppConfiguration(
    override val token: String = env.get("TGTO_BOT_TOKEN"),
    override val baseUrl: String = env.getOrNull("TGTO_BASE_URL") ?: "http://localhost:8080",
    override val ds: DataSourceConfiguration = DefaultDataSourceConfiguration(),
) : AppConfiguration

/**
 * DataSource Configuration
 *
 * @author Ruslan Ibragimov
 */
interface DataSourceConfiguration {
    val url: String
    val username: String
    val password: String
    val driverClassName: String
}

/**
 * Implementation that uses env as default variables
 *
 * @author Ruslan Ibragimov
 */
class DefaultDataSourceConfiguration(
    override val url: String = env.getOrNull("TGTO_JDBC_URL") ?: "jdbc:postgresql://tgto_database:5432/tgto",
    override val username: String = "tgto",
    override val password: String = "tgto",
    override val driverClassName: String = "org.postgresql.Driver"
) : DataSourceConfiguration
