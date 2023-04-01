package cs346.shared

/**
 * This object is responsible for holding sorting functions.
 */
object Sort {
    /**
     * This class is used to indicate to sorting functions the order of the sort.
     * ASC = Ascending
     * DESC = Descending
     */
    enum class Order {
        ASC, DESC
    }

    /**
     * Sorts [notes] by title in the [order] order
     *
     * @param notes is the list of notes to be sorted
     * @param order is the order the notes should be sorted by
     *
     * @return an immutable sorted list of notes
     */
    fun sortByTitle(notes: List<Note>, order: Order): List<Note> {
        return when (order) {
            Order.ASC -> notes.sortedWith(compareBy { it.title })
            Order.DESC -> notes.sortedWith(compareByDescending { it.title })
        }
    }

    /**
     * Sorts [notes] by date modified in the [order] order
     *
     * @param notes is the list of notes to be sorted
     * @param order is the order the notes should be sorted by
     *
     * @return an immutable sorted list of notes
     */
    fun sortByDateModified(notes: List<Note>, order: Order): List<Note> {
        return when (order) {
            Order.ASC -> notes.sortedWith(compareBy { it.dateModified })
            Order.DESC -> notes.sortedWith(compareByDescending { it.dateModified })
        }
    }

    /**
     * Sorts [notes] by date created in the [order] order
     *
     * @param notes is the list of notes to be sorted
     * @param order is the order the notes should be sorted by
     *
     * @return an immutable sorted list of notes
     */
    fun sortByDateCreated(notes: List<Note>, order: Order): List<Note> {
        return when (order) {
            Order.ASC -> notes.sortedWith(compareBy { it.dateCreated })
            Order.DESC -> notes.sortedWith(compareByDescending { it.dateCreated })
        }
    }

    /**
     * Sorts [notes] by id in the [order] order
     *
     * @param notes is the list of notes to be sorted
     * @param order is the order the notes should be sorted by
     *
     * @return an immutable sorted list of notes
     */
    fun sortByID(notes: List<Note>, order: Order): List<Note> {
        return when (order) {
            Order.ASC -> notes.sortedWith(compareBy { it.id })
            Order.DESC -> notes.sortedWith(compareByDescending { it.id })
        }
    }
}