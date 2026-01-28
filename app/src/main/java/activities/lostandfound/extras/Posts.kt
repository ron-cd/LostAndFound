package activities.lostandfound.extras

// Use Strings for URLs and IDs from Firestore
data class Posts(
    val id: String? = null,
    val imageURL: String? = null,
    var itemName: String = "",
    var place: String = "",
    var date: String = "",
    var approved: Boolean = false,
    var found : Boolean = false,
    var notified : Boolean = false
)