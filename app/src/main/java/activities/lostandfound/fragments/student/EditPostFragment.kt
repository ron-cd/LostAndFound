package activities.lostandfound.fragments.student

import activities.lostandfound.studentview.MainHome
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentEditPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class EditPostFragment : Fragment() {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var currentDocId: String? = null


    companion object {
        var selectedImageUrl: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = resources.getStringArray(R.array.categories)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.autoComplete.setAdapter(arrayAdapter)


        // 1. Load data from Firestore
        loadPostData()

        // 2. Handle Update Button
        binding.updateButton.setOnClickListener {
            updatePostData()
        }
    }

    private fun loadPostData() {
        val url = selectedImageUrl ?: return

        db.collection("posts")
            .whereEqualTo("imageURL", url)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    currentDocId = doc.id // Store this for the update query

                    // Populate fields
                    binding.ETItemName.setText(doc.getString("itemName"))
                    binding.ETDate.setText(doc.getString("date"))
                    binding.ETPlace.setText(doc.getString("place"))
                    binding.ETDescription.setText(doc.getString("description"))
                    binding.autoComplete.setText(doc.getString("category"), false)

                    // Load Image
                    Glide.with(requireContext()).load(url).into(binding.editImage)
                }
            }
    }

    private fun updatePostData() {
        val docId = currentDocId ?: return
        val user = FirebaseAuth.getInstance().currentUser

        // Collect new data from UI
        val updatedData = mapOf(
            "itemName" to binding.ETItemName.text.toString(),
            "description" to binding.ETDescription.text.toString(),
            "category" to binding.autoComplete.text.toString(),
            "place" to binding.ETPlace.text.toString(),
            "approved" to false,
            "notified" to false,
            "found" to false,
            "imageURL" to selectedImageUrl,
            "date" to binding.ETDate.text.toString(),
        )

        binding.updateButton.isEnabled = false

        db.collection("posts").document(docId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(context, "Post updated successfully, please wait again for approval", Toast.LENGTH_SHORT).show()
                (activity as? MainHome)?.binding?.pager?.setCurrentItem(2, true)
            }
            .addOnFailureListener {
                binding.updateButton.isEnabled = true
                Toast.makeText(context, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}