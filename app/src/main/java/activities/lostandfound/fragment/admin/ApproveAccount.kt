package activities.lostandfound.fragment.admin

import activities.lostandfound.extras.ApprovePostAdapter
import activities.lostandfound.extras.Posts
import activities.lostandfound.extras.RedeemAdapter
import activities.lostandfound.extras.RedeemRequest
import activities.lostandfound.extras.UserAdapter
import activities.lostandfound.extras.Users
import android.content.Intent
import android.net.Uri
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
    private lateinit var tvTitle: TextView
    private var mode = 0
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_approve_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.RV_Accounts)
        tvEmptyState = view.findViewById(R.id.TV_EmptyState)
        tvTitle = view.findViewById(R.id.textView16)
        val headerLayout = view.findViewById<View>(R.id.header_layout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        view.findViewById<View>(R.id.header_layout).setOnClickListener {
            mode = (mode + 1) % 3
            refreshView()
        }

        refreshView()
    }

    private fun refreshView() {
        recyclerView.adapter = null
        when(mode) {
            0 -> { tvTitle.text = "Approve Account"; loadPendingUsers() }
            1 -> { tvTitle.text = "Approve Posts"; loadPendingPosts() }
            2 -> { tvTitle.text = "Redeem Requests"; loadRedeemRequests() }
        }
    }

    private fun loadRedeemRequests() {
        db.collection("redeems")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, error ->
                // 1. Check for errors first
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                // 2. Ensure we only process if the mode is still correct
                if (mode != 2) return@addSnapshotListener

                val list = mutableListOf<RedeemRequest>()

                // 3. Only clear and fill if snapshots is not null
                if (snapshots != null && !snapshots.isEmpty) {
                    for (doc in snapshots) {
                        val req = doc.toObject(RedeemRequest::class.java)
                        req.id = doc.id
                        list.add(req)
                    }

                    recyclerView.adapter = RedeemAdapter(list,
                        onApprove = { req -> processRedeem(req, true) },
                        onReject = { req -> processRedeem(req, false) }
                    )
                    updateUI(false, "") // Hide empty state
                } else {
                    // 4. Only show empty state if snapshots are actually empty
                    updateUI(true, "No pending redemptions found.")
                }
            }
    }

    private fun processRedeem(request: RedeemRequest, isApproved: Boolean) {
        val ref = db.collection("redeems").document(request.id!!)
        if (isApproved) {
            ref.update("status", "claimed").addOnSuccessListener {
                sendEmail(request.claimerEmail, "Claim Approved", "Your claim for ${request.itemName} is approved.")
            }
        } else {
            ref.delete().addOnSuccessListener {
                sendEmail(request.claimerEmail, "Claim Rejected", "Your claim for ${request.itemName} was rejected.")
            }
        }
    }

    private fun sendEmail(recipient: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(intent, "Send Email..."))
    }

    private fun loadPendingUsers() {
        db.collection("users").whereEqualTo("approved", false)
            .addSnapshotListener { snapshots, _ ->
                val userList = mutableListOf<Users>()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val user = doc.toObject(Users::class.java)
                        if (user.email != "NUSdao@admin.nu-clark.edu.ph") {
                            user.id = doc.id
                            userList.add(user)
                        }
                    }
                    recyclerView.adapter = UserAdapter(userList,
                        onApprove = { user -> updateAccountStatus(user, true) },
                        onReject = { user -> updateAccountStatus(user, false) }
                    )
                    updateUI(userList.isEmpty(), "No pending requests found.")
                }
            }
    }

    private fun loadPendingPosts() {
        db.collection("posts").whereEqualTo("approved", false)
            .addSnapshotListener { snapshots, _ ->
                val postList = mutableListOf<Posts>()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val post = doc.toObject(Posts::class.java)
                        post.documentId = doc.id // Mapping ID
                        postList.add(post)
                    }


                    recyclerView.adapter = ApprovePostAdapter(
                        postList,
                        onApprove = { post -> updatePostStatus(post, true) },
                        onReject = { post -> updatePostStatus(post, false) }
                    )
                    updateUI(postList.isEmpty(), "No pending posts found.")
                }
            }
    }

    private fun updateUI(isEmpty: Boolean, message: String) {
        tvEmptyState.text = message
        tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    // Updated updateAccountStatus
    private fun updateAccountStatus(user: Users, isApproved: Boolean) {
        val userId = user.id ?: return
        val ref = db.collection("users").document(userId)

        if (isApproved) {
            ref.update("approved", true)
                .addOnSuccessListener { Toast.makeText(context, "Account Approved", Toast.LENGTH_SHORT).show() }
        } else {
            // Permanent deletion for rejection
            ref.delete()
                .addOnSuccessListener { Toast.makeText(context, "Account Rejected and Deleted", Toast.LENGTH_SHORT).show() }
        }
    }

    // Updated updatePostStatus
    private fun updatePostStatus(post: Posts, isApproved: Boolean) {
        val postId = post.documentId ?: return
        val ref = db.collection("posts").document(postId)

        if (isApproved) {
            ref.update("approved", true)
                .addOnSuccessListener { Toast.makeText(context, "Post Approved", Toast.LENGTH_SHORT).show() }
        } else {
            // Permanent deletion for rejection
            ref.delete()
                .addOnSuccessListener { Toast.makeText(context, "Post Rejected and Deleted", Toast.LENGTH_SHORT).show() }
        }
    }
}