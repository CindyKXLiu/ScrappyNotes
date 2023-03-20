package cs346.shared

object Sort {
    enum class Order {
        ASC, DESC
    }

    fun sortByTitle(notes: List<Note>, order: Order): List<Note> {
        return when (order) {
            Order.ASC -> notes.sortedWith(compareBy { it.title })
            Order.DESC -> notes.sortedWith(compareByDescending { it.title })
        }
    }

    fun sortByModifiedDate(notes: List<Note>, order: Order): List<Note> {
        return when (order) {
            Order.ASC -> notes.sortedWith(compareBy { it.dateModified })
            Order.DESC -> notes.sortedWith(compareByDescending { it.dateModified })
        }
    }

    fun sortByCreatedDate(notes: List<Note>, order: Order): List<Note> {
        return when (order) {
            Order.ASC -> notes.sortedWith(compareBy { it.dateCreated })
            Order.DESC -> notes.sortedWith(compareByDescending { it.dateCreated })
        }
    }
}