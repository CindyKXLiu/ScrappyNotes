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

    /**
     * Retrieves the Model's state that is saved to database
     *
     * @return the state of the Model that was previously saved to the database
     */
    fun getState(): Model.State {
        val notes = HashMap<UInt, Note>()
        val groups = HashMap<String, Group>()

        transaction {
            // create groups
            var query = GroupsTable.selectAll()
            query.forEach{
                val group = Group(it[GroupsTable.groupName])
                groups[it[GroupsTable.groupName]] = group
            }

            // create notes and put them in their corresponding groups (if they belong to a group)
            query = NotesTable.selectAll().orderBy(NotesTable.noteId to SortOrder.ASC) // is sorted ascending so that internal note counter used for generating id aligns with database
            query.forEach {
                //create note obj
                val note = Note(it[NotesTable.title], it[NotesTable.content])
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
     * Saves [state] to database
     *
     * @param state contains the state of the Model
     */
    fun saveState(state: Model.State) {
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
     * Clears all entries the database
     */
    fun clear() {
        transaction {
            NotesTable.deleteAll()
            GroupsTable.deleteAll()
        }
    }
}