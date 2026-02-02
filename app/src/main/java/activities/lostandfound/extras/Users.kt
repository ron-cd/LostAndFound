package activities.lostandfound.extras

data class Users(
var id: String? = null, // To store the Firestore Document ID
val username: String = "",
val email: String = "",
val password: String = "",
val approved: Boolean = false
){
    constructor() : this(null, "", "", "", false)
}
