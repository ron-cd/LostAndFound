package com.example.lostandfound

import AddPostsAdapter
import activities.lostandfound.fragments.student.EditPostFragment
import activities.lostandfound.fragments.student.NewPostFragment
import activities.lostandfound.studentview.MainHome
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.databinding.FragmentPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private val imageList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.addpost.layoutManager = GridLayoutManager(requireContext(), 2)
        loadImagesFromFirestore()

        // Handle FAB click
        binding.fabAdd.setOnClickListener {
            binding.root.animate()
                .alpha(0f)
                .setDuration(450)
                .withEndAction {
                    (activity as? MainHome)?.binding?.pager?.setCurrentItem(4, true)
                    binding.root.animate().alpha(1f).setDuration(450).start()
                }
                .start()
        }


    }


    private fun loadImagesFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // Use addSnapshotListener instead of .get()
        db.collection("posts")
            .whereEqualTo("approved", true)
            .whereEqualTo("ownerId", user.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Handle error
                    toggleEmptyState(isEmpty = true)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    imageList.clear()
                    for (doc in snapshots) {
                        doc.getString("imageURL")?.let { imageList.add(it) }
                    }

                    toggleEmptyState(isEmpty = false)

                    // If you already have an adapter, just notify it.
                    // Otherwise, initialize it:
                    if (binding.addpost.adapter == null) {
                        binding.addpost.adapter = AddPostsAdapter(imageList) { url ->
                            openEditScreen(url)
                        }
                    } else {
                        binding.addpost.adapter?.notifyDataSetChanged()
                    }
                } else {
                    toggleEmptyState(isEmpty = true)
                }
            }
    }

    // Helper function to keep your code clean
    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.addpost.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.IVPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.textView7.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }


    private fun openEditScreen(imageUrl: String) {
        EditPostFragment.selectedImageUrl = imageUrl

        binding.root.animate()
            .translationX(-binding.root.width.toFloat() / 2)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                (activity as? MainHome)?.binding?.pager?.setCurrentItem(5, false)
                binding.root.translationX = binding.root.width.toFloat() / 2
                binding.root.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .start()
            }
            .start()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
