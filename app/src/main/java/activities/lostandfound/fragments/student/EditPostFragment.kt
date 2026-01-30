package activities.lostandfound.fragments.student

import activities.lostandfound.studentview.MainHome
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentEditPostBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment responsible for editing an existing lost and found post.
 */
class EditPostFragment : Fragment() {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var currentDocId: String? = null

    companion object {
        var selectedImageUrl: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Back Gesture/Button Management ---
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Redirects to Index 2 of the ViewPager instead of closing
                    (activity as? MainHome)?.binding?.pager?.setCurrentItem(2, true)
                }
            }
        )

        // UI Initialization
        setupCategoryDropdown()
        loadPostData()

        // Listeners
        binding.updateButton.setOnClickListener {
            updatePostData()
        }
    }

    /**
     * Configures the autocomplete dropdown for categories.
     */
    private fun setupCategoryDropdown() {
        val categories = resources.getStringArray(R.array.categories)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.autoComplete.setAdapter(arrayAdapter)
    }

    /**
     * Retrieves the existing post data from Firestore using the image URL as a reference.
     */
    private fun loadPostData() {
        val url = selectedImageUrl ?: return

        db.collection("posts")
            .whereEqualTo("imageURL", url)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    currentDocId = doc.id

                    // Map Firestore data to UI fields
                    binding.ETItemName.setText(doc.getString("itemName"))
                    binding.ETDate.setText(doc.getString("date"))
                    binding.ETPlace.setText(doc.getString("place"))
                    binding.ETDescription.setText(doc.getString("description"))
                    binding.autoComplete.setText(doc.getString("category"), false)

                    // Display image
                    Glide.with(requireContext()).load(url).into(binding.editImage)
                }
            }
    }

    /**
     * Updates the Firestore document with the newly modified data.
     */
    private fun updatePostData() {
        val docId = currentDocId ?: return

        val updatedData = mapOf(
            "itemName" to binding.ETItemName.text.toString(),
            "description" to binding.ETDescription.text.toString(),
            "category" to binding.autoComplete.text.toString(),
            "place" to binding.ETPlace.text.toString(),
            "approved" to false,
            "notified" to false,
            "found" to false,
            "imageURL" to selectedImageUrl,
            "date" to binding.ETDate.text.toString()
        )

        binding.updateButton.isEnabled = false

        db.collection("posts").document(docId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(context, "Post updated successfully, please wait again for approval", Toast.LENGTH_SHORT).show()
                (activity as? MainHome)?.binding?.pager?.setCurrentItem(2, true)
            }
            .addOnFailureListener { e ->
                binding.updateButton.isEnabled = true
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        setupCategoryDropdown()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}