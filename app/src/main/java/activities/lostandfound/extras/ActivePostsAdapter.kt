package activities.lostandfound.extras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R

class ActivePostsAdapter(private var postList : ArrayList<Posts>) : RecyclerView.Adapter<ActivePostsAdapter.MyViewHolder>() {
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
        Glide.with(holder.itemView.context)
            .load(currentItem.imageURL)
            .placeholder(R.drawable.progress_icon)
            .error(R.drawable.photo_placeholder)
            .centerCrop()
            .into(holder.photoIcon)

        if (currentItem.found == true) {
            holder.status.visibility = View.VISIBLE
        } else {
            holder.status.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            // Handle the click here
            val context = holder.itemView.context
            Toast.makeText(context, "Clicked: ${currentItem.itemName}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return postList.size
    }

    fun searchDataList(searchList: List<Posts>){
        this.postList = ArrayList(searchList)
        notifyDataSetChanged()
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val photoIcon: ImageView = itemView.findViewById(R.id.IV_photoicon)
        val itemName: TextView = itemView.findViewById(R.id.TV_Itemname)
        val place: TextView = itemView.findViewById(R.id.TV_Place)
        val date: TextView = itemView.findViewById(R.id.TV_Date)
        val status : TextView = itemView.findViewById(R.id.TV_Status)

    }
}