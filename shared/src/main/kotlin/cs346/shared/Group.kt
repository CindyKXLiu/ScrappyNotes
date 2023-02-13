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
}