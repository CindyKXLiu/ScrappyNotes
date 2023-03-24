package cs346.shared

import com.google.gson.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


private const val DB_URL = "jdbc:sqlite:model.db"
private const val VARCHAR_LENGTH = 10000

private const val BASE_URL = "http://localhost:8080"

/**
 * This class is responsible for storing and retrieving Model state from the database.
 *
 * @constructor creates a database at jdbc:sqlite:notes.db containing an empty state table
 */
internal class ModelDatabase(){
    /**
     * Upon init ModelDatabase will connect to the database and create a NotesTable
     */
    init {
        println("local init called")
        Database.connect(DB_URL)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(NotesTable)
            SchemaUtils.create(GroupsTable)
        }

        try {
            // attempt to connect and retrieve web service state
            val response = getStateWebService()

            if (response.second == 200) {
                // if there is a local database instance, get local state
                val stateLocal: Model.State = getStateLocal()

                // sync web service and local states
                var newState: Model.State = syncStates(response.first, stateLocal)

                // clear local and web service dbs
                clear()
                // update local and web service dbs
                saveState(newState)
            }
        } catch (e: ConnectException) {
            // continue without communicating with web service
        }
    }

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

    private fun syncStates(stateWebService: Model.State, stateLocal: Model.State): Model.State {
        var notes = stateWebService.notes
        var groups = stateWebService.groups

        for ((id, note) in stateLocal.notes) {
            if (!notes.containsKey(id) || (notes.containsKey(id) && note.dateModified > notes[id]?.dateModified)) {
                // replace the note with the more recently updated one
                // or add a note that is not in the list yet
                notes[id] = note
            }
        }

        for ((id, group) in stateLocal.groups) {
            if (!groups.containsKey(id)) {
                // add group if it does not exist
                groups[id] = group
            }
        }

        return Model.State(notes, groups)
    }

    // Gson UInt serializer and deserializer
    internal class UintJson : JsonSerializer<UInt>, JsonDeserializer<UInt> {
        override fun serialize(src: UInt, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toLong())
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): UInt {
            return json.asLong.toUInt()
        }
    }

    // Gson LocalDateTime serializer and deserializer
    internal class LocalDateTimeJson : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        override fun serialize(src: LocalDateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString());
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): LocalDateTime {
            return LocalDateTime.parse(json.asString)
        }
    }

    private fun getStateWebService(): Pair<Model.State, Int> {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$BASE_URL/model"))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        println("GET state status code: " + response.statusCode())

        val responseBody: String = response.body()

        val gson = GsonBuilder().registerTypeAdapter(UInt::class.java, UintJson())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeJson()).create()

        return Pair<Model.State, Int>(gson.fromJson(responseBody, Model.State::class.java), response.statusCode())
    }

    private fun getStateLocal(): Model.State {
        val notes = HashMap<UInt, Note>()
        val groups = HashMap<String, Group>()

        transaction {
            // create groups
            var query = GroupsTable.selectAll()
            query.forEach {
                val group = Group(it[GroupsTable.groupName])
                groups[it[GroupsTable.groupName]] = group
            }

            // create notes and put them in their corresponding groups (if they belong to a group)
            query = NotesTable.selectAll()
                .orderBy(NotesTable.noteId to SortOrder.ASC) // is sorted ascending so that internal note counter used for generating id aligns with database
            query.forEach {
                //create note obj
                val note = Note(it[NotesTable.title], it[NotesTable.content], it[NotesTable.noteId].toUInt())
                note.dateCreated = it[NotesTable.dateCreated]
                note.dateModified = it[NotesTable.dateModified]
                if (!it[NotesTable.groupName].isNullOrBlank()) {
                    note.groupName = it[NotesTable.groupName]

                    // add the note to the group
                    groups[it[NotesTable.groupName]]!!.notes.add(it[NotesTable.noteId].toUInt())
                }

                //store note obj in notes
                notes[note.id] = note
            }
        }

        return Model.State(notes, groups)
    }

    /**
     * Retrieves the Model's state that is saved to database
     *
     * @return the state of the Model that was previously saved to the database
     */
    fun getState(): Model.State {
        // try getting state from web service
        try {
            val response = getStateWebService()

            if (response.second == 200) {
                return response.first
            }
        } catch (e: ConnectException) {
            // continue without communicating with web service
        }

        // get from local database instance otherwise
        return getStateLocal()
    }

    private fun saveStateWebService(state: Model.State): Int {
        val gson = GsonBuilder().registerTypeAdapter(UInt::class.java, UintJson())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeJson()).create()
        val stateJSON = gson.toJson(state, Model.State::class.java)

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$BASE_URL/model"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(stateJSON))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        println("POST state status code: " + response.statusCode())

        return response.statusCode()
    }

    private fun saveStateLocal(state: Model.State) {
        transaction {
            // save notes to NotesTable
            for ((id, note) in state.notes) {
                NotesTable.insert {
                    it[noteId] = id.toInt()
                    it[title] = note.title
                    it[content] = note.content
                    it[dateCreated] = note.dateCreated
                    it[dateModified] = note.dateModified
                    if (!note.groupName.isNullOrBlank()) {
                        it[groupName] = note.groupName
                    }
                }
            }

            // save groups to GroupsTable
            for ((name, _) in state.groups) {
                GroupsTable.insert {
                    it[groupName] = name
                }
            }
        }
    }

    /**
     * Saves [state] to database
     *
     * @param state contains the state of the Model
     */
    fun saveState(state: Model.State) {
        // update both web service and local if possible
        try {
            saveStateWebService(state)
        } catch (e: ConnectException) {
            // continue without communicating with web service
        }

        // update local regardless
        saveStateLocal(state)
    }

    private fun clearWebService(): Int {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$BASE_URL/model"))
            .DELETE()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        println("DELETE state status code: " + response.statusCode())

        return response.statusCode()
    }

    private fun clearLocal() {
        transaction {
            NotesTable.deleteAll()
            GroupsTable.deleteAll()
        }
    }

    /**
     * Clears all entries the database
     */
    fun clear() {
        // update both web service and local if possible
        try {
            clearWebService()
        } catch (e: ConnectException) {
            // continue without communicating with web service
        }

        // update local regardless
        clearLocal()
    }
}