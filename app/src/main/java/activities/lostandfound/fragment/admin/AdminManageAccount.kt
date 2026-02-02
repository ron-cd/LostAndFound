package activities.lostandfound.fragment.admin

import activities.lostandfound.extras.UserAdapter
import activities.lostandfound.extras.Users
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

// Ensure you have a User data class and a UserAdapter created!
class AdminManageAccount : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private var userList = mutableListOf<Users>()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var searchField: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_admin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize UI
        recyclerView = view.findViewById(R.id.RV_Accounts) // Make sure this ID matches your XML
        searchField = view.findViewById(R.id.ET_Search2)
        setupRecyclerView()

        // 2. Fetch Data
        loadApprovedUsers()

        // 3. Attach Swipe Logic
        setupSwipeToDelete()

        setupSearchLogic()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(userList,
            onApprove = { user -> updateStatus(user, true) },
            onReject = { user -> updateStatus(user, false) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadApprovedUsers() {
        db.collection("users")
            .whereEqualTo("approved", true)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    userList.clear()
                    for (doc in snapshots) {
                        val user = doc.toObject(Users::class.java)

                        // Filter out the Admin email here
                        if (user.email != "NUSdao@admin.nu-clark.edu.ph") {
                            user.id = doc.id
                            userList.add(user)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun setupSearchLogic() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchList(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun searchList(text: String) {
        val filteredList = if (text.isBlank()) {
            userList
        } else {
            userList.filter { post ->
                post.username.contains(text, ignoreCase = true)
            }
        }
        adapter.searchDataList(ArrayList(filteredList))
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedUser = userList[position]

                userList.removeAt(position)
                adapter.notifyItemRemoved(position)

                Snackbar.make(recyclerView, "Deleted ${deletedUser.username}", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        userList.add(position, deletedUser)
                        adapter.notifyItemInserted(position)
                    }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                deleteFromFirestore(deletedUser.id)
                            }
                        }
                    })
                    .show()
            }

            // --- THIS PART DRAWS THE RED BACKGROUND AND ICON ---
            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                // 1. Draw Red Background
                val background = android.graphics.drawable.ColorDrawable(android.graphics.Color.RED)
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                // 2. Draw Delete Icon
                val icon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.delete_icon) // Ensure this ID exists!
                val iconMargin = (itemHeight - icon!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemHeight - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight

                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    private fun deleteFromFirestore(userId: String?) {
        userId?.let {
            db.collection("users").document(it).delete()
                .addOnFailureListener { e -> Log.e("Firestore", "Error deleting", e) }
        }
    }

    private fun updateStatus(user: Users, isApproved: Boolean) {
        val userId = user.id ?: return
        val docRef = db.collection("users").document(userId)

        if (isApproved) {
            docRef.update("approved", true)
                .addOnSuccessListener {
                    Toast.makeText(context, "${user.username} Approved!", Toast.LENGTH_SHORT).show()
                }
        } else {
            // For rejection, we usually just delete the signup request
            docRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Request Rejected", Toast.LENGTH_SHORT).show()
                }
        }
    }
}