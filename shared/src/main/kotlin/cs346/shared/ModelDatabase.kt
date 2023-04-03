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

private const val DB_URL = "jdbc:sqlite:model.db"
private const val VARCHAR_LENGTH = 10000
private const val BASE_URL = "http://localhost:8080"

/**
 * This class is responsible for storing and retrieving Model state from the database.
 *
 * @constructor creates a database at jdbc:sqlite:notes.db containing an empty state table
 */
internal class ModelDatabase(){
    private object NotesTable : Table("Notes") {
        val noteId: Column<UUID> = uuid("noteId")
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

    /**
     * Upon init ModelDatabase will connect to the database and create a NotesTable
     */
    init {
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
                val newState: Model.State = syncStates(response.first, stateLocal)

                // clear local and web service dbs
                clear()
                // update local and web service dbs
                saveState(newState)
            }
        } catch (e: ConnectException) {
            // continue without communicating with web service
        }
    }

    /**
     * Merge the states of the web service and local databases. If there are duplicate
     * notes, take the note with the newest dateModified.
     *
     * @param stateWebService contains the state retrieved from the web service database
     * @param stateLocal contains the state retrieved from the local database
     *
     * @return a combined state object
     */
    fun syncStates(stateWebService: Model.State, stateLocal: Model.State): Model.State {
        val notes = stateWebService.notes
        val groups = stateWebService.groups

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

    // Gson UUID serializer and deserializer
    private class UUIDAdapter : JsonSerializer<UUID>, JsonDeserializer<UUID> {
        override fun serialize(src: UUID, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
            return JsonPrimitive(src.toString())
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): UUID? {
            return UUID.fromString(json.asString)
        }
    }

    // Gson LocalDateTime serializer and deserializer
    private class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        override fun serialize(src: LocalDateTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString())
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): LocalDateTime {
            return LocalDateTime.parse(json.asString)
        }
    }

    /**
     * Retrieve the state object from the web service database via HTTP
     *
     * @return a pair containing the state of the model from the web service and the response status code
     */
    fun getStateWebService(): Pair<Model.State, Int> {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$BASE_URL/model"))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        val responseBody: String = response.body()

        val gson = GsonBuilder().registerTypeAdapter(UUID::class.java, UUIDAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter()).create()

        return Pair<Model.State, Int>(gson.fromJson(responseBody, Model.State::class.java), response.statusCode())
    }

    /**
     * Retrieve the state object from the local database
     *
     * @return the state of the model from the local database
     */
    private fun getStateLocal(): Model.State {
        val notes = HashMap<UUID, Note>()
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
                val note = Note(it[NotesTable.title], it[NotesTable.content])
                note.id = it[NotesTable.noteId]
                note.dateCreated = it[NotesTable.dateCreated]
                note.dateModified = it[NotesTable.dateModified]
                if (!it[NotesTable.groupName].isNullOrBlank()) {
                    note.groupName = it[NotesTable.groupName]

                    // add the note to the group
                    groups[it[NotesTable.groupName]]!!.notes.add(it[NotesTable.noteId])
                }

                //store note obj in notes
                notes[note.id] = note
            }
        }

        return Model.State(notes, groups)
    }

    /**
     * Retrieve the Model's state from the web service if it is accessible. Otherwise,
     * retrieve the Model's state from the local database
     *
     * @return the state of the Model that was previously saved
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

    /**
     * Saves [state] to the web service database
     *
     * @param state contains the state of the Model
     *
     * @return the status code of the HTTP request
     */
    private fun saveStateWebService(state: Model.State): Int {
        val gson = GsonBuilder().registerTypeAdapter(UUID::class.java, UUIDAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter()).create()

        val stateJSON = gson.toJson(state, Model.State::class.java)

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$BASE_URL/model"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(stateJSON))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.statusCode()
    }

    /**
     * Saves [state] to the local database
     *
     * @param state contains the state of the model
     */
    private fun saveStateLocal(state: Model.State) {
        transaction {
            // save notes to NotesTable
            for ((id, note) in state.notes) {
                NotesTable.insert {
                    it[noteId] = id
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
     * Saves [state] to both the web service and local databases if possible. Otherwise,
     * [state] is saved only to the local database.
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

    /**
     * Deletes all notes and groups in the web service database state
     *
     * @return the status code of the HTTP request
     */
    private fun clearWebService(): Int {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$BASE_URL/model"))
            .DELETE()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.statusCode()
    }

    /**
     * Deletes all notes and groups in the local database state
     */
    private fun clearLocal() {
        transaction {
            NotesTable.deleteAll()
            GroupsTable.deleteAll()
        }
    }

    /**
     * Clears all entries the web service and local databases if possible. Otherwise,
     * clears all entries in the local database only.
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