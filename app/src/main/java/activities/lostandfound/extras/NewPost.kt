package activities.lostandfound.extras

/**
 * Data model representing a new lost or found post entry.
 */
data class NewPost(
    private val id: String = "",
    private val email: String = "",
    private val photoIcon: String = "",
    private val itemName: String = "",
    private val category: String = "",
    private val place: String = "",
    private val date: String = "",
    private val approved: Boolean = false,
    private val found: Boolean = false,
    var notified: Boolean = false
)