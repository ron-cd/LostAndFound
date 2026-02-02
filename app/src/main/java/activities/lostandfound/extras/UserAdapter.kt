package activities.lostandfound.extras
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R

class UserAdapter(
    private var users: List<Users>,
    private val onApprove: (Users) -> Unit,
    private val onReject: (Users) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.TV_Username)
        val tvEmail: TextView = view.findViewById(R.id.TV_Email)
        val tvPassword: TextView? = view.findViewById(R.id.TV_Password)
        val btnApprove: View? = view.findViewById(R.id.BTN_Approve)
        val btnReject: View? = view.findViewById(R.id.BTN_Reject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_approve_account, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUsername.text = user.username
        holder.tvEmail.text = user.email

        holder.tvPassword?.text = user.password

        if (user.approved) {
            holder.btnApprove?.visibility = View.GONE
            holder.btnReject?.visibility = View.GONE // Use this for "Delete" if you want
        } else {
            holder.btnApprove?.visibility = View.VISIBLE
            holder.btnReject?.visibility = View.VISIBLE
        }

        holder.btnApprove?.setOnClickListener { onApprove(user) }
        holder.btnReject?.setOnClickListener { onReject(user) }
    }

    override fun getItemCount() = users.size


    @SuppressLint("NotifyDataSetChanged")
    fun searchDataList(searchList: List<Users>) {
        this.users = ArrayList(searchList)
        notifyDataSetChanged()
    }
}