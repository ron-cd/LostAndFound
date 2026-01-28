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
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.PostFragment
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentNewPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    private var uri: Uri? = null
    private var imageURL: String? = null
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val client = OkHttpClient()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = resources.getStringArray(R.array.categories)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.autoComplete.setAdapter(arrayAdapter)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        binding.ETDate.setText(currentDate)

        val photoIcon = view.findViewById<ImageView>(R.id.uploadImage)
        val itemName = view.findViewById<TextView>(R.id.ET_itemName)
        val category = view.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        val date = view.findViewById<TextView>(R.id.ET_Date)
        val place = view.findViewById<TextView>(R.id.ET_Place)
        val description = view.findViewById<TextView>(R.id.ET_Description)
        val uploadButton = view.findViewById<TextView>(R.id.uploadButton)

        // Initialize Image Picker Launcher
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                uri = data?.data
                binding.uploadImage.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Open Gallery
        binding.uploadImage.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }

        binding.uploadButton.setOnClickListener {
            if (uri != null) {
                // Check if fields are empty before uploading
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


    private fun uploadImageToImgBB(fileUri: Uri) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // 1. Prepare the file
        val file = uriToFile(fileUri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", "0ce4803b31e244218a3c5d525197ecc9") // Replace with your actual key
            .addFormDataPart("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        // 2. Execute Request
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

                    val responseData = it.body?.string()
                    val jsonObject = JSONObject(responseData)
                    val url = jsonObject.getJSONObject("data").getString("url")

                    activity?.runOnUiThread {
                        imageURL = url
                        uploadToFirestore(dialog)
                    }
                }
            }
        })
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    private fun uploadToFirestore(dialog: AlertDialog) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser // Get current user

        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val postData = hashMapOf(
            "ownerId" to user.uid,              // The student's ID
            "ownerEmail" to user.email,        // Helpful for Admin view
            "itemName" to binding.ETItemName.text.toString(),
            "description" to binding.ETDescription.text.toString(),
            "category" to binding.autoComplete.text.toString(),
            "place" to binding.ETPlace.text.toString(),
            "approved" to false,
            "found" to false,
            "imageURL" to imageURL,
            "date" to binding.ETDate.text.toString(),
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("posts")
            .add(postData)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Post Published!", Toast.LENGTH_SHORT).show()

                (activity as? MainHome)?.binding?.pager?.currentItem = 2


                binding.ETItemName.text?.clear()
                binding.ETDescription.text?.clear()
                binding.ETPlace.text?.clear()
                binding.autoComplete.text?.clear()
                binding.uploadImage.setImageResource(R.drawable.photo_placeholder) // Replace with your placeholder ID
                uri = null
            }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        }


    }
