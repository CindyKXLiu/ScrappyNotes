package cs346.webservice

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

private const val VARCHAR_LENGTH = 10000
private const val DB_URL = "jdbc:sqlite:data/model.db"
object ModelDatabase {
    init {
        Database.connect(DB_URL)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(NotesTable)
            SchemaUtils.create(GroupsTable)
        }
    }
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

    /**
     * Retrieves the Model's state that is saved to database
     *
     * @return the state of the Model that was previously saved to the database
     */
    fun getState(): State {
        val notes = HashMap<UUID, Note>()
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
                note.id = it[NotesTable.noteId]
                if (!it[NotesTable.groupName].isNullOrBlank()) {
                    note.groupName = it[NotesTable.groupName]

                    // add the note to the group
                    groups[it[NotesTable.groupName]]!!.notes.add(it[NotesTable.noteId])
                }

                //store note obj in notes
                notes[note.id] = note
            }
        }

        return State(notes, groups)
    }

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

    /**
     * Saves [state] to database
     *
     * @param state contains the state of the Model
     */
    fun saveState(state: State) {
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
                NotesTable.deleteWhere{ noteId eq id }
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
}