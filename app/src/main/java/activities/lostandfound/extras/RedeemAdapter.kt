package activities.lostandfound.extras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R

class RedeemAdapter(
    private val requests: List<RedeemRequest>,
    private val onApprove: (RedeemRequest) -> Unit,
    private val onReject: (RedeemRequest) -> Unit
) : RecyclerView.Adapter<RedeemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.TV_RedeemItemName)
        val email: TextView = view.findViewById(R.id.TV_ClaimerEmail)
        val ivOriginal: ImageView = view.findViewById(R.id.IV_OriginalImage)
        val ivProof: ImageView = view.findViewById(R.id.IV_ProofImage)
        val btnApprove: Button = view.findViewById(R.id.BTN_ApproveRedeem)
        val btnReject: Button = view.findViewById(R.id.BTN_RejectRedeem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_redeem_request, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = requests[position]
        holder.itemName.text = req.itemName
        holder.email.text = req.claimerEmail

        Glide.with(holder.itemView.context).load(req.originalItemURL).placeholder(R.drawable.progress_icon).into(holder.ivOriginal)
        Glide.with(holder.itemView.context).load(req.proofImageURL).placeholder(R.drawable.progress_icon).into(holder.ivProof)

        holder.btnApprove.setOnClickListener { onApprove(req) }
        holder.btnReject.setOnClickListener { onReject(req) }
    }

    override fun getItemCount() = requests.size
}