package activities.lostandfound.extras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R

class RecyclerViewAdapter(private val postList : ArrayList<Posts>) : RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.lost_item_,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
       val currentItem = postList[position]

        holder.itemName.text = currentItem.itemName
        holder.place.text = currentItem.place
        holder.date.text = currentItem.date.toString()
        holder.status.text = currentItem.status

        // Use Glide to load the URL into your ImageView
        Glide.with(holder.itemView.context)
            .load(currentItem.photoIcon)
            .placeholder(R.drawable.photo_placeholder) // Show this while loading
            .into(holder.photoIcon)
    }

    override fun getItemCount(): Int {
        return postList.size
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val photoIcon: ImageView = itemView.findViewById(R.id.IV_photoicon)
        val itemName: TextView = itemView.findViewById(R.id.TV_Itemname)
        val place: TextView = itemView.findViewById(R.id.TV_Place)
        val date: TextView = itemView.findViewById(R.id.TV_Date)
        val status: TextView = itemView.findViewById(R.id.TV_Status)


    }
}