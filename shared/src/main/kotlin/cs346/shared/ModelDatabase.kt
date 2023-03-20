package cs346.shared

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

private const val DB_URL = "jdbc:sqlite:notes.db"
private const val VARCHAR_LENGTH = 10000

/**
 * This class is responsible for storing and retrieving Model states from the database.
 *
 * @property url is the url of the database storing Model states
 * @property varchar_length is the length for the varchar columns
 *
 * @constructor creates a database at [url] containing an empty notes table and an empty groups table
 */
internal class ModelDatabase (val url: String = DB_URL, val varchar_length: Int = VARCHAR_LENGTH){
    object NotesTable : Table("Notes") {
        val noteId: Column<Int> = integer("note_id")
        val title: Column<String> = varchar("title", VARCHAR_LENGTH)
        val content: Column<String> = text("content")
        val dateCreated: Column<LocalDateTime> = datetime("dateCreated")
        val dateModified: Column<LocalDateTime> = datetime("dateModified")
        override val primaryKey = PrimaryKey(noteId, name = "note_id")
    }

    object GroupsTable : Table("Groups") {
        val noteId: Column<Int> = integer("note_id")
        val groupName: Column<String> = varchar("group_name", VARCHAR_LENGTH)
        override val primaryKey = PrimaryKey(noteId, name = "note_id")
    }

    /**
     * Upon init Controller will connect to the database and update itself so that its states (notes and groups)
     * match the states stored in the database
     */
    init {
        print("init called")
        Database.connect(DB_URL)

        transaction {
            addLogger(StdOutSqlLogger)

            // create tables
            SchemaUtils.create(NotesTable)
            SchemaUtils.create(GroupsTable)
        }
    }

    /**
     * Retrieves the notes that are saved to database
     *
     * @return a hashmap of notes that are in the database
     */
    fun getNotesState(): HashMap<UInt, Note> {
        print("get notes")
        val notes = HashMap<UInt, Note>()

        transaction {
            val query = NotesTable.selectAll()
                .orderBy(NotesTable.noteId to SortOrder.ASC) // is sorted ascending so that internal note counter used for generating id aligns with database
            query.forEach {
                //create note obj, Controller.creatNote() is not called since it saves to UndoStack
                val note = Note(it[NotesTable.title], it[NotesTable.content])
                note.dateCreated = it[NotesTable.dateCreated]
                note.dateModified = it[NotesTable.dateModified]

                //store note obj in Controller.notes
                notes[note.id] = note
            }
        }

        return notes
    }

    /**
     * Retrieves the groups that are saved to database
     *
     * @return a hashmap of groups that are in the database
     */
    fun getGroupsState(): HashMap<String, Group>{
        val groups = HashMap<String, Group>()

        transaction {
            val query = GroupsTable.selectAll()
            query.forEach {
                if (groups.containsKey(it[GroupsTable.groupName])) { // group named it[GroupsTable.groupName] already exists in local model
                    // add note with id it[GroupsTable.noteId] to that group
                    groups[it[GroupsTable.groupName]]!!.notes.add(it[GroupsTable.noteId].toUInt())
                } else { // group named it[GroupsTable.groupName] does not exist in local model
                    // create group named it[GroupsTable.groupName] containing note with id it[GroupsTable.noteId]
                    val group = Group(it[GroupsTable.groupName])
                    group.notes.add(it[GroupsTable.noteId].toUInt())
                    // add group to Controller.groups
                    groups[it[GroupsTable.groupName]] = group
                }
            }
        }

        return groups
    }

    /**
     * Saves [notes] to database
     *
     * @param notes contains the notes to be saved in the database.
     */
    fun saveNotes(notes: HashMap<UInt, Note>) {
        transaction {
            for ((id, note) in notes) {
                NotesTable.insert {
                    it[noteId] = id.toInt()
                    it[title] = note.title
                    it[content] = note.content
                    it[dateCreated] = note.dateCreated
                    it[dateModified] = note.dateModified
                }
            }
        }
    }

    /**
     * Saves [groups] to database
     *
     * @param groups contains the groups to be saved in the database.
     */
    fun saveGroups(groups: HashMap<String, Group>) {
        transaction {
            for ((name, group) in groups) {
                for (id in group.notes) {
                    GroupsTable.insert{
                        it[noteId] = id.toInt()
                        it[groupName] = name
                    }
                }
            }
        }
    }

    /**
     * Clears all entries in all tables in the database
     */
    fun clearDatabase() {
        transaction {
            NotesTable.deleteAll()
            GroupsTable.deleteAll()
        }
    }
}