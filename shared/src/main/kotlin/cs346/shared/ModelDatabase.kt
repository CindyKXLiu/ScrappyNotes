package cs346.shared

import com.google.gson.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
internal class ModelDatabase{
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

        // sync local db with webservice db
        syncAndUpdate()
    }

// Table Structures /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private object NotesTable : Table("Notes") {
        val noteId: Column<UUID> = uuid("noteId")
        val noteHash: Column<Int> = integer("noteHash")
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Gson Serializers and Deserializers /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Helper functions /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a hashmap of hashes in NotesTable
     *
     * @return a hashmap where the key is the uuid of the note and the value is the hash of the note
     */
    private fun getNotesTableHash(): HashMap<UUID, Int> {
        val result = HashMap<UUID, Int>()

        transaction {
            // create notes and put them in their corresponding groups (if they belong to a group)
            val query = NotesTable.selectAll()
            query.forEach {
                result[it[NotesTable.noteId]] = it[NotesTable.noteHash]
            }
        }

        return result
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Database functions /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Merge the states [state1] and [state2].
     * If there are duplicate notes, take the note with the newest dateModified.
     *
     * @param state1 contains some state
     * @param state2 contains some state
     *
     * @return a combined state object
     */
    private fun syncStates(state1: Model.State, state2: Model.State): Model.State {
        val notes = state1.notes
        val groups = state1.groups

        for ((id, note) in state2.notes) {
            if (!notes.containsKey(id)) { // note is in local db but not in webservice db
                notes[id] = note
            } else if (notes[id].hashCode() != note.hashCode() && note.dateModified > notes[id]!!.dateModified) { // note in local db is newer than note in webservice db
                notes[id] = note
            }
        }

        for ((id, group) in state2.groups) {
            if (!groups.containsKey(id)) {
                // add group if it does not exist
                groups[id] = group
            }
        }

        return Model.State(notes, groups)
    }

    /**
     * Will sync the webservice database and the local database,
     * and will update the webservice database and the local database with the synced state.
     *
     * @ return the synced state
     */
    fun syncAndUpdate(): Model.State {
        try {
            val response = getStateWebService()
            if (response.second != 200) {
                throw ConnectException()
            }
            val syncedState = syncStates(response.first, getStateLocal())
            saveState(syncedState)
        } catch (e: ConnectException) {}

        // webservice can not be accessed, no sync and update were performed, returns the local state
        return getStateLocal()
    }
    /**
     * Retrieve the state object from the web service database via HTTP
     *
     * @return a pair containing the state of the model from the web service and the response status code
     */
    private fun getStateWebService(): Pair<Model.State, Int> {
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
     * Retrieve the Model's state from the web service if it is accessible.
     * Otherwise, retrieve the Model's state from the local database.
     * If web service state is accessible, sync it with the local state and return the sync state.
     *
     * @return the state of the Model that was previously saved
     */
    fun getState(): Model.State {
        var webserviceState:Model.State? = null

        // try getting state from web service
        try {
            val response = getStateWebService()

            if (response.second == 200) {
                webserviceState = response.first
            }
        } catch (e: ConnectException) {}

        // if a webservice state is accessible sync with local and return that
        if (webserviceState != null) {
            return syncStates(webserviceState, getStateLocal())
        }

        // else get the local state
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
            val notesTableHash = getNotesTableHash()

            for ((id, note) in state.notes) {
                if ( !notesTableHash.containsKey(id) ) { // This note was newly created
                    NotesTable.insert {
                        it[noteId] = id
                        it[title] = note.title
                        it[content] = note.content
                        it[dateCreated] = note.dateCreated
                        it[dateModified] = note.dateModified
                        it[noteHash] = note.hashCode()
                        it[groupName] = note.groupName
                    }
                } else if ( note.hashCode() != notesTableHash[id] ) { // this note was changed thus it need to be updated
                    NotesTable.update({ NotesTable.noteId eq id}) {
                        it[title] = note.title
                        it[content] = note.content
                        it[dateCreated] = note.dateCreated
                        it[dateModified] = note.dateModified
                        it[noteHash] = note.hashCode()
                        it[groupName] = note.groupName
                    }
                }

                notesTableHash.remove(id)
            }
            // delete the entries that are still in notesTableHash, since they are not in the local state, these notes were deleted
            for ((id, _) in notesTableHash) {
                NotesTable.deleteWhere{ NotesTable.noteId eq id }
            }

            // save groups to GroupsTable
            GroupsTable.deleteAll()
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
        } catch (e: ConnectException) {}

        // update local regardless
        saveStateLocal(state)
    }

/**
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
    }**/
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}