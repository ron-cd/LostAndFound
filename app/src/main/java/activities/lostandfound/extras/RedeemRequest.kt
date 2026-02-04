package activities.lostandfound.extras

data class RedeemRequest(
    var id: String? = null,
    val claimerEmail: String = "",
    val claimerUid: String = "",
    val itemName: String = "",
    val originalItemURL: String = "",
    val proofImageURL: String = "",
    val status: String = ""
)