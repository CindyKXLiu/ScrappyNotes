package cs346.shared

import java.time.Instant

/**
 * This class is responsible for the logic behind the app.
 *
 * @property notes is a hashmap containing all existing notes in the app, it is keyed by its creation date
 * @property groups is a hashmap contains all existing groups in the app, it is keyed by its name
 *
 * @constructor creates a controller with no elements in notes and groups
 */
class Controller() {
    private var notes: HashMap<Instant, Note> = HashMap<Instant, Note>()
    private var groups: HashMap<String, Group> = HashMap<String, Group>()

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
        notes[newNote.dateCreated] = newNote
        return newNote
    }

    /**
     * Delete the given note.
     *
     * @param createdDate is the date the note was created, the creation date of the note is used as its unique id
     */
    fun deleteNote(createdDate: Instant) {
        // Check that the note given exists
        if (!notes.containsKey(createdDate)) return

        // Remove note from their groups
        for ((_, group) in groups) {
            group.removeNote(createdDate)
        }

        // Delete note from controller
        notes.remove(createdDate)
    }

    /**
     * Modifies the title of the given note.
     *
     * @param createdDate is the date the note was created, the creation date of the note is used as its unique id
     * @param title is the new title of the note
     */
    fun editNoteTitle(createdDate: Instant, title:String = "") {
        // Check that the note given exists
        if (!notes.containsKey(createdDate)) return

        //Edit note
        notes[createdDate]!!.title = title
    }

    /**
     * Modifies the title of the given note.
     *
     * @param createdDate is the date the note was created, the creation date of the note is used as its unique id
     * @param content is the new content of the note
     */
    fun editNoteContent(createdDate: Instant, content:String = "") {
        // Check that the note given exists
        if (!notes.containsKey(createdDate)) return

        //Edit note
        notes[createdDate]!!.content = content
    }

    /**
     * Create a group with a given name
     *
     * @param name is the name of the group
     *
     */
    fun createGroup(name: String) {
        val newGroup = Group(name)
        groups[name] = newGroup
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
        // Delete the group
        deleteGroup(currentName)
        // Add a group with a new name and the same content
        groups[newName] = groupContent
    }

    /**
     * Adds a note to a given group
     *
     * @param name is the name of the group
     * @param note is the note to be added to the group
     *
     */
    fun addNoteToGroup(name: String, note: Note) {
        // Check that the group given exists
        if (!groups.containsKey(name)) return

        // Call the addNote function
        groups[name]!!.addNote(note)
    }

    /**
     * Adds a hashmap of notes to a given group
     *
     * @param name is the name of the group
     * @param notes are the notes to be added to the group
     *
     */
    fun addNotesToGroup(name: String, notes: HashMap<Instant, Note>) {
        // Check that the group given exists
        if (!groups.containsKey(name)) return

        // Call the addNote function
        groups[name]!!.addNotes(notes)
    }

    /**
     * Removes a note from a given group
     *
     * @param name is the name of the group
     * @param note is the note to be removed from the group
     *
     */
    fun removeNoteFromGroup(name: String, note: Note) {
        // Check that the group given exists
        if (!groups.containsKey(name)) return

        // Call the removeNote function
        groups[name]!!.removeNote(note.dateCreated)
    }

    /**
     * Removes a hashmap of notes to a given group
     *
     * @param name is the name of the group
     * @param notes are the notes to be removed to the group
     *
     */
    fun removeNotesFromGroup(name: String, notes: HashMap<Instant, Note>) {
        // Check that the group given exists
        if (!groups.containsKey(name)) return

        groups[name]!!.removeNotes(notes)
    }

    /**
     * Moves a note from one group to another
     *
     * @param oldGroup is the name of the group the note is currently in
     * @param newGroup is tne name of the group to move the note to
     * @param note is the note in question
     *
     */
    fun moveNoteBetweenGroups(oldGroup: String, newGroup: String, note: Note) {
        // Check that the old and new groups given exist
        if (!groups.containsKey(oldGroup) || !groups.containsKey(newGroup)) return

        // Add the note to the new group
        addNoteToGroup(newGroup, note)
        // Remove the note from the old group
        removeNoteFromGroup(oldGroup, note)
    }

    /**
     * Moves a hashmap of notes from one group to another
     *
     * @param oldGroup is the name of the group the notes is currently in
     * @param newGroup is tne name of the group to move the notes to
     * @param notes is the hashmap of notes in question
     *
     */
    fun moveNotesBetweenGroups(oldGroup: String, newGroup: String, notes: HashMap<Instant, Note>) {
        // Check that the old and new groups given exist
        if (!groups.containsKey(oldGroup) || !groups.containsKey(newGroup)) return

        addNotesToGroup(newGroup, notes)
        removeNotesFromGroup(oldGroup, notes)
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
     * Returns the requested note.
     *
     * @param createdDate is the date the note was created, the creation date of the note is used as its unique id
     * @return the note that was requested, null will be returned if no such not exists
     */
    fun getNoteByDateCreated(createdDate: Instant): Note? {
        if (!notes.containsKey(createdDate)) return null
        return notes[createdDate]
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