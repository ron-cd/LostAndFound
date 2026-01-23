package activities.lostandfound.extras

// Use Strings for URLs and IDs from Firestore
data class Posts(
    var photoIcon: String = "",
    var itemName: String = "",
    var place: String = "",
    var date: Any? = null,
    var status: String = ""
)