package io.heapy.tgto.dao

import jetbrains.exodus.entitystore.Entity
import kotlinx.coroutines.runBlocking
import kotlinx.dnq.*
import kotlinx.dnq.query.size
import kotlinx.dnq.query.toList
import kotlinx.dnq.store.container.StaticStoreContainer
import kotlinx.dnq.util.initMetaData
import java.io.File

class XdMessage(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdMessage>()

    var created by xdRequiredDateTimeProp()
    var id by xdRequiredStringProp()
    var text by xdRequiredStringProp()
    var user: XdUser by xdParent(XdUser::messages)
}

class XdUser(entity: Entity) : XdEntity(entity) {
    companion object : XdNaturalEntityType<XdUser>()

    val messages by xdChildren0_N(XdMessage::user)
    var tgId by xdRequiredStringProp()
    var url by xdRequiredStringProp()
}

fun migrate(userDao: UserDao, messageDao: MessageDao) {
    XdModel.registerNodes(XdMessage, XdUser)
    val xodusStore = StaticStoreContainer.init(
        dbFolder = File("./.xodus-dnq-data"),
        environmentName = "db"
    )
    initMetaData(XdModel.hierarchy, xodusStore)
    xodusStore.transactional(readonly = true) {
        XdUser.all().toList().let { users ->
            println("users: ${users.size}")
            users.forEachIndexed { idx, user ->
                runBlocking {
                    val skip = user.messages.size() == 1 || user.messages.size() > 100
                    if (skip) {
                        println("Skipping user ${user.tgId}, messages = ${user.messages.size()}")
                        return@runBlocking
                    }
                    userDao.create(
                        UserDTO(
                            url = user.url,
                            tgId = user.tgId
                        )
                    )
                    user.messages.toList().let { messages ->
                        messages.forEach { message ->
                            messageDao.insert(MessageDTO(
                                id = message.id,
                                text = message.text,
                                created = message.created.millis,
                                tgId = user.tgId
                            ))
                        }
                    }
                }
            }
        }
    }
    xodusStore.close()
}
