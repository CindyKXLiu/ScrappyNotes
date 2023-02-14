package cs346.shared

import java.time.Instant

/**
 * This class is a for containing notes within the same group
 *
 * @property name is the name of the group
 * @property notes is a hashmap of notes belonging to the group, it is keyed by its creation date
 *
 * @constructor creates an empty group
 */
internal class Group(var name: String) {
    val notes: HashMap<Instant, Note> = HashMap<Instant, Note>()

    /**
     * Add a note to the group
     *
     * @param note is the note object
     *
     */
    fun addNote(note: Note) {
        notes[note.dateCreated] = note
    }

    /**
     * Add a hashmap of notes to the group
     *
     * @param addNotes is the hashmap of notes
     *
     */
    fun addNotes(addNotes: HashMap<Instant, Note>) {
        notes.putAll(addNotes)
    }

    /**
     * Remove a note from the group
     *
     * @param dateCreated is the date the note was created for indexing purposes
     *
     */
    fun removeNote(dateCreated: Instant) {
        // Check that the note given exists
        if (!notes.containsKey(dateCreated)) return

        notes.remove(dateCreated)
    }

    /**
     * Remove a hashmap of notes from the group
     *
     * @param removeNotes is the hashmap of notes
     *
     */
    fun removeNotes(removeNotes: HashMap<Instant, Note>) {
        for ((_, note) in removeNotes) {
            removeNote(note.dateCreated)
        }
    }
}