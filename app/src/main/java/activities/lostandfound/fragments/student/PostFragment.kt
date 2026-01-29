package activities.lostandfound.fragments.student

import activities.lostandfound.extras.AddPostsAdapter
import activities.lostandfound.studentview.MainHome
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.databinding.FragmentPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment responsible for displaying the user's specific approved posts.
 * Includes animations for navigating to the Add and Edit screens.
 */
class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private val imageList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadImagesFromFirestore()
    }

    /**
     * Configures the grid layout for displaying post images.
     */
    private fun setupRecyclerView() {
        binding.addpost.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    /**
     * Handles interactions for the Floating Action Button.
     */
    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            // Fade out animation before switching to Add Post tab
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

    /**
     * Listens for real-time updates from Firestore filtered by the current user.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun loadImagesFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("approved", true)
            .whereEqualTo("ownerId", user.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    toggleEmptyState(isEmpty = true)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    imageList.clear()
                    for (doc in snapshots) {
                        doc.getString("imageURL")?.let { imageList.add(it) }
                    }

                    toggleEmptyState(isEmpty = false)

                    // Initialize or update the adapter
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

    /**
     * Updates visibility of the empty state UI and the RecyclerView.
     */
    private fun toggleEmptyState(isEmpty: Boolean) {
        binding.addpost.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.IVPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.textView7.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    /**
     * Prepares data and animates transition to the EditPostFragment.
     */
    private fun openEditScreen(imageUrl: String) {
        // Set the global reference for the edit screen
        EditPostFragment.selectedImageUrl = imageUrl

        // Slide and fade transition
        binding.root.animate()
            .translationX(-binding.root.width.toFloat() / 2)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                (activity as? MainHome)?.binding?.pager?.setCurrentItem(5, false)

                // Reset position for when the user returns
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