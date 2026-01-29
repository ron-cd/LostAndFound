package activities.lostandfound.extras

/**
 * Data model representing a Post item in the system.
 * Designed for compatibility with Firebase Realtime Database or Firestore.
 */
data class Posts(
    val id: String? = null,
    val imageURL: String? = null,
    var itemName: String = "",
    var place: String = "",
    var date: String = "",
    var approved: Boolean = false,
    var found: Boolean = false,
    var notified: Boolean = false
)