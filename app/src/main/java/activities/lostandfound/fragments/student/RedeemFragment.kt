package activities.lostandfound.fragments.student

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentRedeemBinding
import com.google.firebase.firestore.FirebaseFirestore

class RedeemFragment : Fragment() {

    // View Binding is the industry standard for accessing views safely
    private var _binding: FragmentRedeemBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var currentDocId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRedeemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPostData()

    }

    /**
     * Retrieves the existing post data from Firestore using the image URL as a reference.
     */
    @SuppressLint("SetTextI18n")
    private fun loadPostData() {
        val url = HomeFragment.selectedImageUrl ?: return

        db.collection("posts")
            .whereEqualTo("imageURL", url)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    currentDocId = doc.id

                    // 1. Hide Placeholders
                    binding.IVPlaceholder.visibility = View.GONE
                    binding.TVPlaceholder.visibility = View.GONE

                    // 2. Show Detail Views & Labels
                    binding.TVItemName.visibility = View.VISIBLE
                    binding.IVItemImage.visibility = View.VISIBLE
                    binding.TVStatus.visibility = View.VISIBLE
                    binding.TVDateLabel.visibility = View.VISIBLE
                    binding.TVDate.visibility = View.VISIBLE
                    binding.TVCategoryLabel.visibility = View.VISIBLE
                    binding.TVCategory.visibility = View.VISIBLE
                    binding.TVLocationLabel.visibility = View.VISIBLE
                    binding.TVLocation.visibility = View.VISIBLE
                    binding.TVDescriptionLabel.visibility = View.VISIBLE
                    binding.scrollView.visibility = View.VISIBLE
                    binding.TVDescription.visibility = View.VISIBLE


                    // 3. Map Firestore data to UI fields
                    binding.TVItemName.text = doc.getString("itemName")
                    binding.TVDate.text = doc.getString("date")
                    binding.TVLocation.text = doc.getString("place")
                    binding.TVCategory.text = doc.getString("category")
                    binding.TVDescription.text = doc.getString("description")

                    // 4. Handle Status Logic
                    val isFound = doc.getBoolean("found") ?: false
                    if (isFound) {
                        binding.TVStatus.text = "Found"
                        binding.TVStatus.setTextColor(resources.getColor(R.color.green, null))
                    } else {
                        binding.TVStatus.text = "Request"
                        binding.TVStatus.setTextColor(resources.getColor(R.color.green, null))
                    }

                    // 5. Display image
                    Glide.with(requireContext())
                        .load(url)
                        .centerCrop()
                        .into(binding.IVItemImage)
                }
            }
    }

    private fun resetUI() {
        // 1. Clear the stored URL so it doesn't reload the old post automatically
        HomeFragment.selectedImageUrl = null
        currentDocId = null

        binding.apply {
            // 2. Show Placeholders again
            IVPlaceholder.visibility = View.VISIBLE
            TVPlaceholder.visibility = View.VISIBLE

            // 3. Hide all Detail components
            TVItemName.visibility = View.GONE
            IVItemImage.visibility = View.GONE
            TVStatus.visibility = View.GONE
            TVDateLabel.visibility = View.GONE
            TVDate.visibility = View.GONE
            TVCategoryLabel.visibility = View.GONE
            TVCategory.visibility = View.GONE
            TVLocationLabel.visibility = View.GONE
            TVLocation.visibility = View.GONE
            TVDescriptionLabel.visibility = View.GONE
            scrollView.visibility = View.GONE

            // 4. Clear text fields so old data doesn't "flicker" next time
            TVItemName.text = ""
            TVDescription.text = ""
            IVItemImage.setImageDrawable(null) // Clear the previous image
        }
    }

    override fun onPause() {
        super.onPause()
        // This triggers whenever the user swipes away from this tab
        resetUI()
    }



    override fun onResume() {
        super.onResume()
        loadPostData()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevents memory leaks
    }
}