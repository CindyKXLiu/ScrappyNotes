package cs346.shared

import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * This class is responsible for the logic behind the app.
 *
 * @property notes is a hashmap containing all existing notes in the app, it is keyed by its creation date
 * @property groups is a hashmap contains all existing groups in the app, it is keyed by its name
 *
 * @constructor creates a controller with no elements in notes and groups
 */
class Controller {
    private var notes: HashMap<UUID, Note> = HashMap()
    private var groups: HashMap<String, Group> = HashMap()

    /**
     * Converts lists of notes to hashmaps of notes
     *
     * @param notes is a list of notes
     *
     * @return hashmap structure containing notes indexed by date created
     */
    private fun listToHashMapNotes(notes: List<Note>): HashMap<UUID, Note> {
        val notesHashMap = HashMap<UUID, Note>()
        for (note in notes) {
            notesHashMap[note.id] = note
        }

        return notesHashMap
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
        val newNote = Note(title, content)
        notes[newNote.id] = newNote
        return newNote
    }

    /**
     * Delete the given note.
     *
     * @param id is the unique id of the note
     */
    fun deleteNote(id: UUID) {
        // Check that the note given exists
        if (!notes.containsKey(id)) return

        // Remove note from their groups
        for ((_, group) in groups) {
            group.removeNote(id)
        }

        // Delete note from controller
        notes.remove(id)
    }

    /**
     * Modifies the title of the given note.
     *
     * @param id is the unique id of the note
     * @param title is the new title of the note
     */
    fun editNoteTitle(id: UUID, title:String = "") {
        // Check that the note given exists
        if (!notes.containsKey(id)) return

        //Edit note
        notes[id]!!.title = title
    }

    /**
     * Modifies the title of the given note.
     *
     * @param id is the unique id of the note
     * @param content is the new content of the note
     */
    fun editNoteContent(id: UUID, content:String = "") {
        // Check that the note given exists
        if (!notes.containsKey(id)) return

        //Edit note
        notes[id]!!.content = content
    }

    /**
     * Create a group with a given name
     *
     * @param name is the name of the group
     * @param notes is a list of notes to add to the new group
     *
     * @return the group that was created
     *
     */
    fun createGroup(name: String, notes: List<Note> = listOf()): Group {
        val newGroup = Group(name)
        groups[name] = newGroup
        addNotesToGroup(name, notes)
        return newGroup
    }

    /**
     * Delete the group
     * By extension, the notes under the deleted group are not
     * associated with it anymore
     *
     * @param name is the name of the group
     *
     */
    fun deleteGroup(name: String) {
        // Check that the group given exists
        if (!groups.containsKey(name)) return

        // Delete group from controller
        groups.remove(name)
    }

    /**
     * Modifies the name of the given group.
     *
     * @param currentName is the current name of the group
     * @param newName is the new name of the group
     *
     */
    fun editGroupName(currentName: String, newName: String) {
        // Check that the group given exists
        if (!groups.containsKey(currentName)) return

        // Save the content of the group
        val groupContent: Group = groups[currentName] ?: return
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
     * @return a group with the corresponding name if it exists or null
     */
    fun getGroupByName(name: String): Group? {
        if (!groups.containsKey(name)) return null
        return groups[name]
    }

    /**
     * Adds a note to a given group
     *
     * @param groupName is the name of the group
     * @param note is the note to be added to the group
     *
     */
    fun addNoteToGroup(groupName: String, note: Note) {
        // Check that the group given exists
        if (!groups.containsKey(groupName)) return

        // Call the addNote function
        groups[groupName]!!.addNote(note)
    }

    /**
     * Adds a list of notes to a given group
     *
     * @param groupName is the name of the group
     * @param notes is the list of notes to be added to the group
     *
     */
    fun addNotesToGroup(groupName: String, notes: List<Note>) {
        // Check that the group given exists
        if (!groups.containsKey(groupName)) return

        // Convert the list of notes to a hashmap of notes
        val notesHashMap = listToHashMapNotes(notes)

        // Call the addNote function
        groups[groupName]!!.addNotes(notesHashMap)
    }

    /**
     * Removes a note from a given group
     *
     * @param groupName is the name of the group
     * @param note is the note to be removed from the group
     *
     */
    fun removeNoteFromGroup(groupName: String, note: Note) {
        // Check that the group given exists
        if (!groups.containsKey(groupName)) return

        // Call the removeNote function
        groups[groupName]!!.removeNote(note.id)
    }

    /**
     * Removes a list of notes to a given group
     *
     * @param groupName is the name of the group
     * @param notes are the notes to be removed to the group
     *
     */
    fun removeNotesFromGroup(groupName: String, notes: List<Note>) {
        // Check that the group given exists
        if (!groups.containsKey(groupName)) return

        // Convert the list of notes to a hashmap of notes
        val notesHashMap = listToHashMapNotes(notes)

        groups[groupName]!!.removeNotes(notesHashMap)
    }

    /**
     * Moves a note from one group to another
     *
     * @param oldGroupName is the name of the group the note is currently in
     * @param newGroupName is tne name of the group to move the note to
     * @param note is the note in question
     *
     */
    fun moveNoteBetweenGroups(oldGroupName: String, newGroupName: String, note: Note) {
        // Check that the old and new groups given exist
        if (!groups.containsKey(oldGroupName) || !groups.containsKey(newGroupName)) return

        // Add the note to the new group
        addNoteToGroup(newGroupName, note)
        // Remove the note from the old group
        removeNoteFromGroup(oldGroupName, note)
    }

    /**
     * Moves a list of notes from one group to another
     *
     * @param oldGroupName is the name of the group the notes is currently in
     * @param newGroupName is tne name of the group to move the notes to
     * @param notes is the list of notes in question
     *
     */
    fun moveNotesBetweenGroups(oldGroupName: String, newGroupName: String, notes: List<Note>) {
        // Check that the old and new groups given exist
        if (!groups.containsKey(oldGroupName) || !groups.containsKey(newGroupName)) return

        addNotesToGroup(newGroupName, notes)
        removeNotesFromGroup(oldGroupName, notes)
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
                if (group.notes.containsKey(id)) {
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
     * @return the notes that were requested, null will be returned if no such note exists
     */
    fun getNoteByID(id: UUID) : Note? {
        if (!notes.containsKey(id)) return null
        return notes[id]
    }
    /**
     * Returns the notes with the requested [title].
     *
     * @param title is the requested title
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
}