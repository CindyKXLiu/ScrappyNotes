package cs346.shared

import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * This class is responsible for the logic behind the app and models the state of the app.
 *
 * @property database is the database used for persistently storing model state
 * @property notes is a hashmap containing all existing notes in the app, it is keyed by its id
 * @property groups is a hashmap contains all existing groups in the app, it is keyed by its name
 *
 * @constructor creates a Model with states that reflects the stored state in the database
 */
class Model {
    /**
     * This class is a container class for the state of the Model.
     *
     * @property notes is a hashmap containing all existing notes in the app, it is keyed by its id
     * @property groups is a hashmap contains all existing groups in the app, it is keyed by its name
     */
    data class State(val notes: HashMap<UInt, Note>, val groups: HashMap<String, Group>)

    private val database: ModelDatabase = ModelDatabase()
    private var notes: HashMap<UInt, Note> = HashMap()
    private var groups: HashMap<String, Group> = HashMap()

    // Update notes and groups with the database state
    init {
        database.getState().let { state ->
            notes = state.notes
            groups = state.groups
        }
    }

// Database Functionalities /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Saves the current state of the Model to the database
     */
    fun saveToDatabase() {
        database.clear()
        database.saveState(State(notes, groups))
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Undo/Redo Functionalities using Memento pattern///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This object is responsible for undoing and redoing Model function calls
     *
     * @property undoStates is a stack containing the past states of the Model, used for undoing actions
     * @property redoStates is a stock containing the past states popped off via undo commands, used for redoing actions
     */
    private object UndoRedoManager {
        private val undoStates = Stack<State>()
        private val redoStates = Stack<State>()

        /**
         * Saves the given state as a memento to undo stack and clears redo stack
         *
         * @param notes is the current state of the Model's notes property
         * @param groups is the current state of the Model's groups property
         */
        fun saveToUndo(notes: HashMap<UInt, Note>, groups: HashMap<String, Group>) {
            undoStates.push(State(notes, groups))
        }

        /**
         * Saves the given state as a memento to redo stack
         *
         * @param notes is the current state of the Model's notes property
         * @param groups is the current state of the Model's groups property
         */
        fun saveToRedo(notes: HashMap<UInt, Note>, groups: HashMap<String, Group>) {
            redoStates.push(State(notes, groups))
        }

        /**
         * Returns the memento representing the state of the Model before the last function call
         *
         * @exception NoUndoException is thrown when there is no action to be undone
         *
         * @return the memento representing the state of the Model before the last function call
         */
        fun undo(): State {
            if (undoStates.empty()) throw NoUndoException()
            val memento = undoStates.peek()
            undoStates.pop()
            return memento
        }

        /**
         * Returns the memento representing the state of the Model before the undo call
         *
         * @exception NoRedoException is thrown when there is no action to be redone
         * @return the memento representing the state of the Model before the undo call
         */
        fun redo(): State {
            if (redoStates.empty()) throw NoRedoException()
            val memento = redoStates.peek()
            redoStates.pop()
            return memento
        }

        /**
         * Called to clear the redo stack.
         * Redo stack is cleared when a chain of undo has been broken.
         */
        fun resetRedo() {
            redoStates.clear()
        }
    }

    /**
     * Returns a deep copy of the notes property of Model
     *
     * @return a deep copy of the notes property of Model
     */
    private fun notesCopy(): HashMap<UInt, Note> {
        val notesCopy = HashMap<UInt, Note>()
        for ((id, note) in notes) {
            notesCopy[id] = Note(note)
        }
        return notesCopy
    }

    /**
     * Returns a deep copy of the groups property of Model
     *
     * @return a deep copy of the groups property of Model
     */
    private fun groupsCopy(): HashMap<String, Group> {
        val groupsCopy = HashMap<String, Group>()
        for ((name, group) in groups) {
            groupsCopy[name] = Group(group)
        }
        return groupsCopy
    }

    /**
     * Called by state changing functions (with exception to undo/redo) to save the current state of Model
     *  before performing their actions.
     *  Redo stack is cleared as the chain of undo is broken.
     */
    private fun save() {
        UndoRedoManager.resetRedo()
        UndoRedoManager.saveToUndo(notesCopy(), groupsCopy())
    }

    /**
     * Undoes the previously called function
     */
    fun undo() {
        // save current state to redo stack
        UndoRedoManager.saveToRedo(notesCopy(), groupsCopy())

        // undo actions
        val memento = UndoRedoManager.undo()
        notes = memento.notes
        groups = memento.groups
    }

    /**
     * Reverse the last called undo
     */
    fun redo() {
        // Save current state to undo stack
        UndoRedoManager.saveToUndo(notesCopy(), groupsCopy())

        // redo the undo
        val memento = UndoRedoManager.redo()
        notes = memento.notes
        groups = memento.groups
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Note Functionalities /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

        // Delete note from Model
        notes.remove(id)
    }

    /**
     * Modifies the title of the given note and updates the dateModified field of the note.
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
     * Modifies the title of the given note and updates the dateModified field of the note.
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
    fun getNotesByDateCreated(dateCreated: LocalDateTime): List<Note> {
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
            if (note.title.contains(title)){
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Group Functionalities ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Create a group with a given name
     *
     * @param name is the name of the group
     *
     * @exception DuplicateGroupException is thrown if a group with name [name] already exists
     *
     * @return the group that was created
     */
    fun createGroup(name: String): Group {
        save()

        if (groups.containsKey(name)) throw DuplicateGroupException()

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

        // Delete group from Model
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

        // Check that the new name given is unique
        if (groups.containsKey(newName)) throw DuplicateGroupException()

        // Save the content of the group
        val groupContent = groups[currentName]!!
        groupContent.name = newName

        // update all notes with new group name
        val notesInGroup = getAllNotesInGroup(currentName)
        for (note in notesInGroup) {
            note.groupName = newName
        }

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
     * @param noteID is the id of the note to be added to the group
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun addNoteToGroup(groupName: String, noteID: UInt) {
        save()
        if (!groups.containsKey(groupName)) throw NonExistentGroupException()
        groups[groupName]!!.notes.add(noteID)
        getNoteByID(noteID).groupName = groupName
    }

    /**
     * Removes a note from a given group
     *
     * @param groupName is the name of the group
     * @param noteID is the id of the note to be removed from the group
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun removeNoteFromGroup(groupName: String, noteID: UInt) {
        save()
        if (!groups.containsKey(groupName)) throw NonExistentGroupException()
        groups[groupName]!!.notes.remove(noteID)
        getNoteByID(noteID).groupName = null
    }

    /**
     * Moves a note from one group to another
     *
     * @param newGroupName is tne name of the group to move the note to
     * @param noteID is the id of the note in question
     *
     * @exception NonExistentGroupException is thrown if the group given does not exist
     */
    fun moveNoteToGroup(newGroupName: String, noteID: UInt) {
        save()
        val currentNote = getNoteByID(noteID)

        // Check that the new group given exist
        if (!groups.containsKey(newGroupName)) throw NonExistentGroupException()

        // Find the current group of the note
        val oldGroupName: String? = currentNote.groupName

        // Remove the note from the old group, if it belonged to a group
        if (oldGroupName != null) {
            groups[oldGroupName]!!.notes.remove(noteID)
        }

        // Add the note to the new group
        groups[newGroupName]!!.notes.add(noteID)
        currentNote.groupName = newGroupName
    }

    /**
     * Returns a list of notes under that group
     * 
     * @param name is the name of the group
     *
     * @return immutable list of all notes under that group
     */
    fun getAllNotesInGroup(name: String): List<Note> {
        val groupNotes = ArrayList<Note>()

        for (id in getGroupByName(name).notes) {
            notes[id]?.let { groupNotes.add(it) }
        }

        return groupNotes.toList()
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
