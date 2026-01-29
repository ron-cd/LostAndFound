package activities.lostandfound.extras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R

class AddPostsAdapter(
    private val items: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<AddPostsAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edit_post, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = items[position]

        // Load image into the view using Glide
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .into(holder.imageView)

        // Handle the image click interaction
        holder.itemView.setOnClickListener {
            onImageClick(imageUrl)
        }
    }

    override fun getItemCount(): Int = items.size

    // --- ViewHolder class for caching view references ---
    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.galleryImage)
    }
}