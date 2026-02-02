package activities.lostandfound.fragment.admin

import activities.lostandfound.extras.UserAdapter
import activities.lostandfound.extras.Users
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.google.firebase.firestore.FirebaseFirestore

class ApproveAccount : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: UserAdapter
    private var pendingUsersList = mutableListOf<Users>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ensure this layout contains a RecyclerView with ID RV_ApproveAccounts
        return inflater.inflate(R.layout.fragment_approve_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.RV_Accounts)
        tvEmptyState = view.findViewById(R.id.TV_EmptyState)

        setupRecyclerView()
        loadPendingUsers()
    }

    private fun setupRecyclerView() {
        // Pass the list and the click logic to the adapter
        adapter = UserAdapter(pendingUsersList,
            onApprove = { user -> updateStatus(user, true) },
            onReject = { user -> updateStatus(user, false) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadPendingUsers() {
        db.collection("users")
            .whereEqualTo("approved", false)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                if (snapshots != null) {
                    pendingUsersList.clear()
                    for (doc in snapshots) {
                        val user = doc.toObject(Users::class.java)
                        // Filter out Admin so they don't appear in "Approve" list
                        if (user.email != "NUSdao@admin.nu-clark.edu.ph") {
                            user.id = doc.id
                            pendingUsersList.add(user)
                        }
                    }
                    adapter.notifyDataSetChanged()

                    // Toggle visibility logic
                    if (pendingUsersList.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun updateStatus(user: Users, isApproved: Boolean) {
        val userId = user.id ?: return

        if (isApproved) {
            // Approve: Update the 'approved' field to true
            db.collection("users").document(userId)
                .update("approved", true)
                .addOnSuccessListener {
                    Toast.makeText(context, "${user.username} Approved", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Reject: Usually means deleting the pending request/account
            db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "${user.username} Rejected/Deleted", Toast.LENGTH_SHORT).show()
                }
        }
    }
}