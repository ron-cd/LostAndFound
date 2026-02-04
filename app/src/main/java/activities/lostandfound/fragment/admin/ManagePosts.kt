package activities.lostandfound.fragment.admin

import activities.lostandfound.extras.ManagePostsAdapter
import activities.lostandfound.extras.Posts
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.databinding.DialogManagePostBinding
import com.example.lostandfound.databinding.FragmentManagePostsBinding
import com.google.firebase.firestore.FirebaseFirestore


class ManagePosts : Fragment() {

    private var _binding: FragmentManagePostsBinding? = null
    private val binding get() = _binding!!
    private var postList = ArrayList<Posts>()
    private lateinit var searchField: EditText
    private lateinit var myAdapter: ManagePostsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagePostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)

        setupRecyclerView()
        loadImagesFromFirestore()
        setupSearchLogic()

    }

    private fun setupRecyclerView() {
        binding.RVPosts.layoutManager = GridLayoutManager(requireContext(), 2)
    }


    private fun initializeViews(view: View) {
        searchField = view.findViewById(R.id.ET_Search3)

    }


    private fun setupSearchLogic() {

        searchField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // In your Activity or Fragment where the search bar is located:
    private fun filter(text: String) {
        val filteredList = ArrayList<Posts>()

        // Iterate through your full original list (postList)
        for (item in postList) {
            // Search by item name or any other searchable property
            if (item.itemName.lowercase().contains(text.lowercase())) {
                filteredList.add(item)
            }
        }

        // Update the adapter with the filtered list
        myAdapter.searchDataList(filteredList)
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun loadImagesFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("approved", true)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                if (snapshots != null) {
                    // CRITICAL: Clear the list first to prevent duplication
                    postList.clear()

                    for (doc in snapshots) {
                        val post = doc.toObject(Posts::class.java)
                        if (post != null) {
                            // Manually assign the Firestore Document ID
                            post.documentId = doc.id
                            postList.add(post)
                        }
                    }

                    // Initialize or update adapter
                    if (binding.RVPosts.adapter == null) {
                        myAdapter = ManagePostsAdapter(postList) { selectedPost ->
                            showManageDialog(selectedPost)
                        }
                        binding.RVPosts.adapter = myAdapter
                    } else {
                        myAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    private fun showManageDialog(post: Posts) {
        val dialogBinding = DialogManagePostBinding.inflate(layoutInflater)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setView(dialogBinding.root)

        val dialog = builder.create()

        // --- The fix for transparency ---
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 1. Load the Description into the EditText
        dialogBinding.TVDescription.setText(post.description)


        // 2. Load the Image using Glide
        // Make sure you have the Glide dependency in build.gradle
        com.bumptech.glide.Glide.with(requireContext())
            .load(post.imageURL) // Make sure 'imageUrl' matches your Posts class field
            .placeholder(R.drawable.progress_icon) // Optional: show while loading
            .into(dialogBinding.IVPhoto)

        if (post.found) {
            // Change button to red if already found
            dialogBinding.BTNClaim.setBackgroundColor(android.graphics.Color.RED)
            dialogBinding.BTNClaim.setTextColor(android.graphics.Color.WHITE)
            dialogBinding.BTNClaim.setText("Surrendered")
            dialogBinding.BTNClaim.isEnabled = false // Optional: disable it
        }

        // 3. Mark As Found Button
        dialogBinding.BTNClaim.setOnClickListener {
            markPostAsFound(post, dialog)
        }

        // 4. Mark As Returned Button
        dialogBinding.BTNReturned.setOnClickListener {
            showConfirmationDialog(post, dialog)
        }

        dialog.show()
    }

    private fun markPostAsFound(post: Posts, parentDialog: androidx.appcompat.app.AlertDialog) {
        val db = FirebaseFirestore.getInstance()

        if (post.documentId != null) {
            // We update the "status" or "found" field instead of deleting
            db.collection("posts").document(post.documentId!!)
                .update("found", true)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Item marked as found!", Toast.LENGTH_SHORT).show()
                    parentDialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun showConfirmationDialog(post: Posts, parentDialog: androidx.appcompat.app.AlertDialog) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Action")
            .setMessage("Are you sure you want to mark this as returned? This will delete the post.")
            .setPositiveButton("Confirm") { _, _ ->
                deletePost(post)
                parentDialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(post: Posts) {
        val db = FirebaseFirestore.getInstance()

        // Ensure your Posts class has a 'documentId' field to know what to delete
        if (post.documentId != null) {
            db.collection("posts").document(post.documentId!!)
                .delete()
                .addOnSuccessListener {
                    android.widget.Toast.makeText(requireContext(), "Post Removed", android.widget.Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    android.widget.Toast.makeText(requireContext(), "Error deleting", android.widget.Toast.LENGTH_SHORT).show()
                }
        }
    }


}