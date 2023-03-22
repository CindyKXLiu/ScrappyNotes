package cs346.webservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@SpringBootApplication
class WebserviceApplication

private const val DB_URL = "jdbc:sqlite:model.db"
private const val VARCHAR_LENGTH = 10000

private object NotesTable : Table("Notes") {
    val noteId: Column<Int> = integer("noteId")
    val title: Column<String> = varchar("title", VARCHAR_LENGTH)
    val content: Column<String> = text("content")
    val dateCreated: Column<LocalDateTime> = datetime("dateCreated")
    val dateModified: Column<LocalDateTime> = datetime("dateModified")
    val groupName: Column<String?> = varchar("groupName", VARCHAR_LENGTH).nullable()
    override val primaryKey = PrimaryKey(noteId, name = "noteId")
}

private object GroupsTable : Table("Groups") {
    val groupName: Column<String> = varchar("groupName", VARCHAR_LENGTH)
    override val primaryKey = PrimaryKey(groupName, name = "groupName")
}

fun main(args: Array<String>) {
    runApplication<WebserviceApplication>(*args)
    print("init called")
    Database.connect(DB_URL)

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(NotesTable)
        SchemaUtils.create(GroupsTable)
    }
}

@RestController
@RequestMapping("/messages")
class MessageResource(val service: MessageService) {
    @GetMapping
    fun index(): List<Message> = service.findMessages()

    @PostMapping
    fun post(@RequestBody message: Message) {
        service.post(message)
    }
}

data class Message(val id: String, val text: String)

@Service
class MessageService {
    var messages: MutableList<Message> = mutableListOf()

    fun findMessages() = messages
    fun post(message: Message) {
        messages.add(message)
    }
}
