package cs346.shared

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

private const val DB_URL = "jdbc:sqlite:model.db"
private const val VARCHAR_LENGTH = 10000

/**
 * This class is responsible for storing and retrieving Model state from the database.
 *
 * @constructor creates a database at jdbc:sqlite:notes.db containing an empty state table
 */
internal class ModelDatabase(){
    object StateTable : Table("State") {
        val noteId: Column<Int> = integer("note_id")
        val title: Column<String> = varchar("title", VARCHAR_LENGTH)
        val content: Column<String> = text("content")
        val dateCreated: Column<LocalDateTime> = datetime("dateCreated")
        val dateModified: Column<LocalDateTime> = datetime("dateModified")
        val groupName: Column<String?> = varchar("groupName", VARCHAR_LENGTH).nullable()
        override val primaryKey = PrimaryKey(noteId, name = "note_id")
    }

    /**
     * Upon init ModelDatabase will connect to the database and create a StateTable
     */
    init {
        print("init called")
        Database.connect(DB_URL)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(StateTable)
        }
    }

    /**
     * Retrieves the Model's state that is saved to database
     *
     * @return the state of the Model that was previously saved to the database
     */
    fun getState(): Model.State {
        val notes = HashMap<UInt, Note>()
        val groups = HashMap<String, Group>()

        transaction {
            val query = StateTable.selectAll().orderBy(StateTable.noteId to SortOrder.ASC) // is sorted ascending so that internal note counter used for generating id aligns with database

            query.forEach {
                //create note obj, Controller.creatNote() is not called since it saves to UndoStack
                val note = Note(it[StateTable.title], it[StateTable.content])
                note.dateCreated = it[StateTable.dateCreated]
                note.dateModified = it[StateTable.dateModified]

                //store note obj in Controller.notes
                notes[note.id] = note

                // if this note belongs to a group
                if (!it[StateTable.groupName].isNullOrBlank()) {
                    // create group obj if not already created
                    if (!groups.containsKey(it[StateTable.groupName])) {
                        val group = Group(it[StateTable.groupName]!!)
                        groups[it[StateTable.groupName]!!] = group
                    }

                    // add the note to the group
                    groups[it[StateTable.groupName]]!!.notes.add(it[StateTable.noteId].toUInt())
                }
            }
        }

        return Model.State(notes, groups)
    }

    /**
     * Saves [state] to database
     *
     * @param state contains the state of the Model
     */
    fun saveState(state: Model.State) {
        transaction {
            for ((id, note) in state.notes) {
                StateTable.insert {
                    it[noteId] = id.toInt()
                    it[title] = note.title
                    it[content] = note.content
                    it[dateCreated] = note.dateCreated
                    it[dateModified] = note.dateModified

                    // find the group the note belongs to
                    for ((name, group) in state.groups) {
                        if (group.notes.contains(id)) {
                            it[groupName] = name
                        }
                    }
                }
            }
        }
    }

    /**
     * Clears all entries the database
     */
    fun clearDatabase() {
        transaction {
            StateTable.deleteAll()
        }
    }
}