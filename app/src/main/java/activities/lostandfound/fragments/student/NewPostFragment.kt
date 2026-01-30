package activities.lostandfound.fragments.student

import activities.lostandfound.studentview.MainHome
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentNewPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Fragment responsible for creating and uploading a new lost/found post.
 * Handles image selection, ImgBB API integration, and Firestore storage.
 */
class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private var uri: Uri? = null
    private var imageURL: String? = null
    private val client = OkHttpClient()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Redirects to Index 2 of the ViewPager instead of closing
                    (activity as? MainHome)?.binding?.pager?.setCurrentItem(2, true)
                }
            }
        )

        setupDefaultValues()
        setupImagePicker()
        setupClickListeners()
    }

    /**
     * Sets initial values for the date field and category dropdown.
     */
    private fun setupDefaultValues() {
        val categories = resources.getStringArray(R.array.categories)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.autoComplete.setAdapter(arrayAdapter)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        binding.ETDate.setText(dateFormat.format(calendar.time))
    }

    /**
     * Initializes the result launcher for selecting images from the gallery.
     */
    private fun setupImagePicker() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uri = result.data?.data
                binding.uploadImage.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Sets up listeners for image selection and post submission.
     */
    private fun setupClickListeners() {
        binding.uploadImage.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            activityResultLauncher.launch(photoPicker)
        }

        binding.uploadButton.setOnClickListener {
            if (uri != null) {
                if (binding.ETItemName.text.isNullOrEmpty() || binding.ETDescription.text.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    uploadImageToImgBB(uri!!)
                }
            } else {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Uploads the selected image to ImgBB and proceeds to Firestore on success.
     */
    private fun uploadImageToImgBB(fileUri: Uri) {
        val dialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setView(R.layout.progress_layout)
            .create()
        dialog.show()

        val file = uriToFile(fileUri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", "0ce4803b31e244218a3c5d525197ecc9")
            .addFormDataPart("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        activity?.runOnUiThread { dialog.dismiss() }
                        return
                    }

                    val jsonObject = JSONObject(it.body?.string() ?: "")
                    val url = jsonObject.getJSONObject("data").getString("url")

                    activity?.runOnUiThread {
                        imageURL = url
                        uploadToFirestore(dialog)
                    }
                }
            }
        })
    }

    /**
     * Converts Uri to a File object for the network request.
     */
    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    /**
     * Saves the post data to the Firestore database.
     */
    private fun uploadToFirestore(dialog: AlertDialog) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        val postData = hashMapOf(
            "ownerId" to user.uid,
            "ownerEmail" to user.email,
            "itemName" to binding.ETItemName.text.toString(),
            "description" to binding.ETDescription.text.toString(),
            "category" to binding.autoComplete.text.toString(),
            "place" to binding.ETPlace.text.toString(),
            "approved" to false,
            "found" to false,
            "imageURL" to imageURL,
            "notified" to false,
            "date" to binding.ETDate.text.toString(),
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("posts")
            .add(postData)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Post Published!\nWait for approval.", Toast.LENGTH_SHORT).show()

                // Redirect to tab index 2
                (activity as? MainHome)?.binding?.pager?.currentItem = 2
                clearFields()
            }
    }

    /**
     * Resets the UI fields after a successful upload.
     */
    private fun clearFields() {
        binding.apply {
            ETItemName.text?.clear()
            ETDescription.text?.clear()
            ETPlace.text?.clear()
            autoComplete.text?.clear()
            uploadImage.setImageResource(R.drawable.photo_placeholder)
        }
        uri = null
    }

    override fun onResume() {
        super.onResume()
        setupDefaultValues()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}