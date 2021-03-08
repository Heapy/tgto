package io.heapy.tgto.migrate

import com.zaxxer.hikari.HikariDataSource
import io.heapy.tgto.dao.XdMessage
import io.heapy.tgto.dao.XdUser
import io.heapy.tgto.db.tables.daos.MessageDao
import io.heapy.tgto.db.tables.daos.TgUserDao
import jetbrains.exodus.database.TransientEntityStore
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.single
import kotlinx.dnq.query.size
import org.joda.time.DateTime
import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import javax.sql.DataSource
import kotlin.concurrent.thread

fun migrate(store: TransientEntityStore) {
    val jooqConfig = DefaultJooqConfigFactory().config()

    val tgUserDao = TgUserDao(jooqConfig)
    val messageDao = MessageDao(jooqConfig)

    val users = tgUserDao.findAll()
    users.forEach { tgUser ->
        println("Migrating tguser: url=${tgUser.url}, userId=${tgUser.userId}")
        store.transactional {
            XdUser.new {
                this.url = tgUser.url
                this.tgId = tgUser.userId.toString()
            }
        }
    }
    val messages = messageDao.findAll()
    messages.forEach { tgMessage ->
        println("Migrating tgmessage: message=${tgMessage.message.take(50)}, userId=${tgMessage.userId}")
        store.transactional {
            val message = XdMessage.new {
                this.text = tgMessage.message
                this.created = DateTime(tgMessage.created.time)
                this.id = "mg-${tgMessage.id}"
            }
            XdUser.filter { it.tgId.eq(tgMessage.userId.toString()) }
                .single().messages.add(message)
        }
    }

    Thread.sleep(10000)

    println("Pg total messages: ${messages.size}")
    println("Pg total users: ${users.size}")
    store.transactional(readonly = true) {
        println("Xd total messages: ${XdMessage.all().size()}")
        println("Xd total users: ${XdUser.all().size()}")
    }
}

class DefaultJooqConfigFactory {
    fun config(): Configuration {
        return DefaultConfiguration().also {
            it.setSQLDialect(SQLDialect.POSTGRES)
            it.setDataSource(dataSource())
        }
    }

    private fun dataSource(): DataSource {
        val dataSource = HikariDataSource().also {
            it.jdbcUrl = "jdbc:postgresql://tgto_database:5432/tgto"
            it.username = "tgto"
            it.password = "tgto"
            it.driverClassName = "org.postgresql.Driver"
            it.maximumPoolSize = 4
        }

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            dataSource.close()
        })

        return dataSource
    }
}
