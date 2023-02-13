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
        var newNote = Note(title, content)
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
            if (group.notes.containsKey(createdDate)) {
                group.notes.remove(createdDate)
            }
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
        var retNotes = ArrayList<Note>()

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
        var retNotes = ArrayList<Note>()

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
        var unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareBy { it.title })
    }

    /**
     * Returns the list of all notes sorted by title in descending order.
     *
     * @return immutable list of all notes sorted by title in descending order
     */
    fun getSortedNotesByTitleDescending(): List<Note> {
        var unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareByDescending { it.title })
    }

    /**
     * Returns the list of all notes sorted by their modified date in ascending order.
     *
     * @return immutable list of all notes sorted by modified date in ascending order
     */
    fun getSortedNotesByModifiedDateAscending(): List<Note> {
        var unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareBy { it.dateModified })
    }

    /**
     * Returns the list of all notes sorted by their modified date in descending order.
     *
     * @return immutable list of all notes sorted by modified date in descending order
     */
    fun getSortedNotesByModifiedDateDescending(): List<Note> {
        var unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareByDescending { it.dateModified })
    }

    /**
     * Returns the list of all notes sorted by their creation date in ascending order.
     *
     * @return immutable list of all notes sorted by creation date in ascending order
     */
    fun getSortedNotesByCreatedDateAscending(): List<Note> {
        var unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareBy { it.dateCreated })
    }

    /**
     * Returns the list of all notes sorted by their creation date in descending order.
     *
     * @return immutable list of all notes sorted by creation date in descending order
     */
    fun getSortedNotesByCreatedDateDescending(): List<Note> {
        var unsortedNotes = getAllNotes()
        return unsortedNotes.sortedWith(compareByDescending { it.dateCreated })
    }
}