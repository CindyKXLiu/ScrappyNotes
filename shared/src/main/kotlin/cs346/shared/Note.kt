package cs346.shared

import java.time.Instant

/**
 * This is the Note class, it will contain the data of a note.
 *
 * @property id is the unique id of the note
 * @property title is the title of the note, this title does not have to be unique
 * @property content is the content of the note
 * @property dateCreated is the time the note is created, this will be used as a unique identifier for the note
 *  as we are assuming that notes are created at unique instantaneous points on the time-line.
 * @property dateModified is the time the note was last modified
 *
 * @constructor creates a note with the given [title] and [content]
 */
class Note(title: String = "", content: String = "") {
    var id: UInt = getID()
        private set
    var dateCreated: Instant = Instant.now()
        private set
    var dateModified: Instant = Instant.now()
        private set
    var title = title
        /**
         * Setter for title, will update the time the note was last modified
         *
         * @param value is the new title of the note
         */
        internal set (value) {
            field = value
            dateModified = Instant.now()
        }
    var content = content
        /**
         * Setter for content, will update the time the note was last modified
         *
         * @param value is the new content of the note
         */
        internal set (value) {
            field = value
            dateModified = Instant.now()
        }

    constructor(note: Note) : this(note.title, note.content) {
        this.id = note.id
        this.dateCreated = note.dateCreated
        this.dateModified = note.dateModified
    }

    /**
     * Static counter for generating "unique" note ids
     */
    private companion object UniqueID {
        var noteCounter = -1
        fun getID() : UInt{
            ++noteCounter
            return noteCounter.toUInt()
        }
    }
}