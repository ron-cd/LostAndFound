package activities.lostandfound.extras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R

class ApprovePostAdapter(
    private val posts: List<Posts>,
    private val onApprove: (Posts) -> Unit,
    private val onReject: (Posts) -> Unit
) : RecyclerView.Adapter<ApprovePostAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.IV_photoicon)
        val name: TextView = view.findViewById(R.id.TV_Title)
        val desc: TextView = view.findViewById(R.id.TV_Description)
        val btnApprove: Button = view.findViewById(R.id.BTN_Approve)
        val btnReject: Button = view.findViewById(R.id.BTN_Reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_approve_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.name.text = post.itemName
        holder.desc.text = post.description

        com.bumptech.glide.Glide.with(holder.itemView.context)
            .load(post.imageURL)
            .placeholder(R.drawable.progress_icon)
            .into(holder.image)

        holder.btnApprove.setOnClickListener { onApprove(post) }
        holder.btnReject.setOnClickListener { onReject(post) }
    }

    override fun getItemCount() = posts.size
}