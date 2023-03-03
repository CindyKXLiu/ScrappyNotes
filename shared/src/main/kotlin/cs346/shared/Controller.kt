package cs346.shared

import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * This class is responsible for the logic behind the app.
 *
 * @property notes is a hashmap containing all existing notes in the app, it is keyed by its id
 * @property groups is a hashmap contains all existing groups in the app, it is keyed by its name
 *
 * @constructor creates a controller with no elements in notes and groups
 */
class Controller {
    private var notes: HashMap<UInt, Note> = HashMap()
    private var groups: HashMap<String, Group> = HashMap()

    /**
     * This class is responsible for holding a past state of the Controller
     */
    private data class Memento(val notes: HashMap<UInt, Note>, val groups: HashMap<String, Group>)

    /**
     * This class is responsible for undoing and redoing Controller function calls
     *
     * @property mementos is a stack containing the past states of the Controller
     */
    private object UndoManager {
        private val mementos = Stack<Memento>()

        /**
         * Saves the given state as a memento
         *
         * @param notes is the current state of the Controller's notes property
         * @param groups is the current state of the Controller's groups property
         */
        fun save(notes: HashMap<UInt, Note>, groups: HashMap<String, Group>) {
            mementos.push(Memento(notes, groups))
        }

        /**
         * Returns the last memento to be stored
         *
         * @return last memento to be stored
         */
        fun undo(): Memento {
            val lastMemento = mementos.peek()
            mementos.pop()
            return lastMemento
        }
    }

    /**
     * Saves the current state of Controller.notes and Controller.groups
     */
    private fun save() {
        // creates a deep copy of notes
        val notesCopy = HashMap<UInt, Note>()
        for ((id, note) in notes) {
            notesCopy[id] = Note(note)
        }

        // creates a deep copy of groups
        val groupsCopy = HashMap<String, Group>()
        for ((name, group) in groups) {
            groupsCopy[name] = Group(group)
        }

        UndoManager.save(notesCopy, groupsCopy)
    }

    /**
     * Undoes the previously called function
     */
    fun undo() {
        val memento = UndoManager.undo()
        notes = memento.notes
        groups = memento.groups
    }

    /**
     * Returns all existing notes
     *
     * @return immutable list of all existing notes in unsorted order
     */
    fun getAllNotes(): List<Note> {
        return notes.values.toList()
    }

    /**
     * Returns all notes that do not belong to a group
     *
     * @return immutable list of all notes that do not belong to a group
     */
    fun getAllUngroupedNotes(): List<Note> {
        val ungroupedNotes = ArrayList<Note>()

        for((id, note) in notes) {
            var belongToGroup = false
            for((_, group) in groups) {
                if (group.notes.contains(id)) {
                    belongToGroup = true
                    break
                }
            }
            if (!belongToGroup) {
                ungroupedNotes.add(note)
            }
        }

        return ungroupedNotes.toList()
    }

    /**
     * Create a note with the given title and content.
     *
     * @param title is the title of the note
     * @param content is the content of the note
     *
     * @return the note that was created
     */
    fun createNote(title: String = "", content: String = ""): Note {
        save()
        val newNote = Note(title, content)
        notes[newNote.id] = newNote
        return newNote
    }

    /**
     * Delete the given note.
     *
     * @param id is the unique id of the note
     *
     * @exception NonExistentNoteException is thrown when no such note with id [id] exits
     */
    fun deleteNote(id: UInt) {
        save()
        // Check that the note given exists
        if (!notes.containsKey(id)) throw NonExistentNoteException()

        // Remove note from their groups
        for ((_, group) in groups) {
            group.notes.remove(id)
        }

        // Delete note from controller
        notes.remove(id)
    }

    /**
     * Modifies the title of the given note.
     *
     * @param id is the unique id of the note
     * @param title is the new title of the note
     *
     * @exception NonExistentNoteException is thrown when no such note with id [id] exits
     */
    fun editNoteTitle(id: UInt, title:String = "") {
        save()
        if (!notes.containsKey(id)) throw NonExistentNoteException()
        notes[id]!!.title = title
    }

    /**
     * Modifies the title of the given note.
     *
     * @param id is the unique id of the note
     * @param content is the new content of the note
     *
     * @exception NonExistentNoteException is thrown when no such note with id [id] exits
     */
    fun editNoteContent(id: UInt, content:String = "") {
        save()
        if (!notes.containsKey(id)) throw NonExistentNoteException()
        notes[id]!!.content = content
    }

    /**
     * Returns the requested notes.
     *
     * @param dateCreated is the time that the notes were created
     *
     * @return the notes that were requested, empty list will be returned if no such notes exist
     */
    fun getNotesByDateCreated(dateCreated: Instant): List<Note> {
        val allNotes = getAllNotes()
        return allNotes.filter{ it.dateCreated == dateCreated }
    }

    /**
     * Returns the requested note.
     *
     * @param id is the time that the note was created
     *
     * @exception NonExistentNoteException is thrown when no such note with id [id] exits
     *
     * @return the note that was requested
     */
    fun getNoteByID(id: UInt) : Note {
        if (!notes.containsKey(id)) throw NonExistentNoteException()
        return notes[id]!!
    }

    /**
     * Returns the notes with the requested [title].
     *
     * @param title is the requested title
     *
     * @return immutable list of all notes titled [title]
     */
    fun getNotesByTitle(title: String): List<Note> {
        val retNotes = ArrayList<Note>()
        for ((_, note) in notes) {
            if (note.title == title){
                retNotes.add(note)
            }
        }
        return retNotes.toList()
    }

    /**
     * Returns the notes whose content contains [content].
     *
     * @param content is the requested content
     * @return immutable list of all notes whose content contains [content]
     */
    fun getNotesByContent(content: String): List<Note> {
        val retNotes = ArrayList<Note>()
        for ((_, note) in notes) {
            if (note.content.contains(content)) {
                retNotes.add(note)
            }
        }
        return retNotes.toList()
    }

    /**
     * Returns the list of all notes sorted by title in ascending order.
     *
     * @return immutable list of all notes sorted by title in ascending order
     */
    fun getSortedNotesByTitleAscending(): List<Note> {
        val unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareBy { it.title })
    }

    /**
     * Returns the list of all notes sorted by title in descending order.
     *
     * @return immutable list of all notes sorted by title in descending order
     */
    fun getSortedNotesByTitleDescending(): List<Note> {
        val unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareByDescending { it.title })
    }

    /**
     * Returns the list of all notes sorted by their modified date in ascending order.
     *
     * @return immutable list of all notes sorted by modified date in ascending order
     */
    fun getSortedNotesByModifiedDateAscending(): List<Note> {
        val unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareBy { it.dateModified })
    }

    /**
     * Returns the list of all notes sorted by their modified date in descending order.
     *
     * @return immutable list of all notes sorted by modified date in descending order
     */
    fun getSortedNotesByModifiedDateDescending(): List<Note> {
        val unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareByDescending { it.dateModified })
    }

    /**
     * Returns the list of all notes sorted by their creation date in ascending order.
     *
     * @return immutable list of all notes sorted by creation date in ascending order
     */
    fun getSortedNotesByCreatedDateAscending(): List<Note> {
        val unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareBy { it.dateCreated })
    }

    /**
     * Returns the list of all notes sorted by their creation date in descending order.
     *
     * @return immutable list of all notes sorted by creation date in descending order
     */
    fun getSortedNotesByCreatedDateDescending(): List<Note> {
        val unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareByDescending { it.dateCreated })
    }

    /**
     * Create a group with a given name
     *
     * @param name is the name of the group
     *
     * @return the group that was created
     */
    fun createGroup(name: String): Group {
        save()
        val newGroup = Group(name)
        groups[name] = newGroup
        return newGroup
    }

    /**
     * Delete the group
     * By extension, the notes under the deleted group are not
     * associated with it anymore
     *
     * @param name is the name of the group
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun deleteGroup(name: String) {
        save()

        // Check that the group given exists
        if (!groups.containsKey(name)) throw NonExistentGroupException()

        // Delete group from controller
        groups.remove(name)
    }

    /**
     * Modifies the name of the given group.
     *
     * @param currentName is the current name of the group
     * @param newName is the new name of the group
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun editGroupName(currentName: String, newName: String) {
        save()

        // Check that the group given exists
        if (!groups.containsKey(currentName)) throw NonExistentGroupException()

        // Save the content of the group
        val groupContent = groups[currentName]!!
        groupContent.name = newName

        // Delete the group
        deleteGroup(currentName)

        // Add a group with a new name and the same content
        groups[newName] = groupContent
    }

    /**
     * Returns all existing groups
     *
     * @return immutable list of all existing groups in unsorted order
     */
    fun getAllGroups(): List<Group> {
        return groups.values.toList()
    }

    /**
     * Returns the group given a group name
     *
     * @param name is the name of the group
     *
     * @return the group with the corresponding name
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun getGroupByName(name: String): Group {
        if (!groups.containsKey(name)) throw NonExistentGroupException()
        return groups[name]!!
    }

    /**
     * Adds a note to a given group
     *
     * @param groupName is the name of the group
     * @param note is the note to be added to the group
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun addNoteToGroup(groupName: String, note: Note) {
        save()
        if (!groups.containsKey(groupName)) throw NonExistentGroupException()
        groups[groupName]!!.notes.add(note.id)
    }

    /**
     * Removes a note from a given group
     *
     * @param groupName is the name of the group
     * @param note is the note to be removed from the group
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun removeNoteFromGroup(groupName: String, note: Note) {
        save()
        if (!groups.containsKey(groupName)) throw NonExistentGroupException()
        groups[groupName]!!.notes.remove(note.id)
    }

    /**
     * Moves a note from one group to another
     *
     * @param newGroupName is tne name of the group to move the note to
     * @param note is the note in question
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun moveNoteToGroup(newGroupName: String, note: Note) {
        save()

        // Check that the new group given exist
        if (!groups.containsKey(newGroupName)) throw NonExistentGroupException()

        // find the current group of the note
        var oldGroupName: String? = null
        for ((groupName, group) in groups) {
            if (group.notes.contains(note.id)) oldGroupName = groupName
        }

        // Add the note to the new group
        groups[newGroupName]!!.notes.add(note.id)

        // Remove the note from the old group, if it belonged to a group
        if (oldGroupName != null) {
            groups[oldGroupName]!!.notes.remove(note.id)
        }
    }
}