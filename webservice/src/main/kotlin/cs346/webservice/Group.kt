package cs346.webservice

/**
 * This class is for containing notes within the same group
 *
 * @property name is the name of the group
 * @property notes is a set of notes ids of notes belonging to the group
 *
 * @constructor creates an empty group
 */
class Group(var name: String) {
    internal val notes: MutableSet<UInt> = mutableSetOf()

    constructor(group: Group): this(group.name) {
        this.notes.addAll(group.notes)
    }

    /**
     * Returns an immutable list of note ids belonging to the group
     *
     * @return immutable list of note ids belonging to the group
     */
    fun getNotes(): Set<UInt> {
        return notes.toSet()
    }

    /**
     * To string used for displaying notes in treeview
     */
    override fun toString(): String {
        return this.name
    }
}