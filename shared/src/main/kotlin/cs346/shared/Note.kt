package cs346.shared

import java.time.LocalDateTime

/**
 * This is the Note class, it will contain the data of a note.
 *
 * @property id is the unique id of the note
 * @property title is the title of the note, this title does not have to be unique
 * @property content is the content of the note
 * @property dateCreated is the time the note is created
 * @property dateModified is the time the note was last modified
 *
 * @constructor creates a note with the given [title] and [content]
 */
class Note(title: String = "", content: String = "") {
    var id: UInt = getID()
        internal set
    var dateCreated: LocalDateTime = LocalDateTime.now()
        internal set
    var dateModified: LocalDateTime = LocalDateTime.now()
        internal set
    var title = title
        internal set (value) {
            field = value
            dateModified = LocalDateTime.now()
        }
    var content = content
        /**
         * Setter for content, will update the time the note was last modified
         *
         * @param value is the new content of the note
         */
        internal set (value) {
            field = value
            dateModified = LocalDateTime.now()
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

    /**
     * To string used for displaying notes in Treeview
     */
    override fun toString(): String {
        return this.title
    }
}