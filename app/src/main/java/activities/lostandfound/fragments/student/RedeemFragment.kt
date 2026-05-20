package activities.lostandfound.fragments.student

import activities.lostandfound.extras.Posts
import activities.lostandfound.fragments.student.HomeFragment.Companion.selectedImageUrl
import activities.lostandfound.studentview.MainHome
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentRedeemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RedeemFragment : Fragment() {

    private var _binding: FragmentRedeemBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val client = OkHttpClient()

    private var currentDocId: String? = null
    private var imageUri: Uri? = null
    private lateinit var dialogImageView: ImageView

    // Image Picker Launcher
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            if (::dialogImageView.isInitialized) {
                Glide.with(this).load(it).into(dialogImageView)
            }
        }
    }

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
                    val currentPost = doc.toObject(Posts::class.java)

                    binding.apply {
                        IVPlaceholder.visibility = View.GONE
                        TVPlaceholder.visibility = View.GONE
                        TVItemName.visibility = View.VISIBLE
                        IVItemImage.visibility = View.VISIBLE
                        TVStatus.visibility = View.GONE
                        TVDateLabel.visibility = View.VISIBLE
                        TVDate.visibility = View.VISIBLE
                        TVCategoryLabel.visibility = View.VISIBLE
                        TVCategory.visibility = View.VISIBLE
                        TVLocationLabel.visibility = View.VISIBLE
                        TVLocation.visibility = View.VISIBLE
                        TVDescriptionLabel.visibility = View.VISIBLE
                        scrollView.visibility = View.VISIBLE
                        TVDescription.visibility = View.VISIBLE

                        TVItemName.text = doc.getString("itemName")
                        TVDate.text = doc.getString("date")
                        TVLocation.text = doc.getString("place")
                        TVCategory.text = doc.getString("category")
                        TVDescription.text = doc.getString("description")
                    }

                    val isFound = doc.getBoolean("found") ?: true
                    if (!isFound) {
                        binding.TVStatus.visibility = View.GONE
                    } else {
                        binding.TVStatus.visibility = View.VISIBLE
                        binding.TVStatus.text = "Request"
                        binding.TVStatus.setTextColor(resources.getColor(R.color.green, null))

                        binding.TVStatus.setOnClickListener {
                            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                            val ownerId = doc.getString("ownerId")

                            // CHECK: Is the user trying to redeem their own post?
                            if (currentUserId == ownerId) {
                                Toast.makeText(requireContext(), "You cannot request your own post.", Toast.LENGTH_SHORT).show()
                            } else {
                                currentPost?.let { post -> showPostDetailDialog(post) }
                            }
                        }
                    }

                    Glide.with(requireContext()).load(url).centerCrop().into(binding.IVItemImage)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading post data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPostDetailDialog(post: Posts) {
        val dialog = android.app.Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_redeem_post, null)

        dialogImageView = view.findViewById(R.id.IV_photoProof)
        val btnSubmit = view.findViewById<Button>(R.id.btnRequest)

        // Reset previous selection
        imageUri = null

        dialogImageView.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri == null) {
                Toast.makeText(requireContext(), "Please upload a photo as proof", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                uploadImageToImgBB(imageUri!!, post)
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(view)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    private fun uploadImageToImgBB(fileUri: Uri, post: Posts) {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setView(R.layout.progress_layout)
            .create()
        progressDialog.show()

        if (com.example.lostandfound.BuildConfig.IMGBB_API_KEY.isBlank()) {
            progressDialog.dismiss()
            Toast.makeText(requireContext(), "Image upload is not configured.", Toast.LENGTH_SHORT).show()
            return
        }

        val file = uriToFile(fileUri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("key", com.example.lostandfound.BuildConfig.IMGBB_API_KEY)
            .addFormDataPart("image", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        activity?.runOnUiThread { progressDialog.dismiss() }
                        return
                    }
                    val jsonObject = JSONObject(it.body?.string() ?: "")
                    val proofUrl = jsonObject.getJSONObject("data").getString("url")

                    activity?.runOnUiThread {
                        saveRedeemToFirestore(proofUrl, post, progressDialog)
                    }
                }
            }
        })
    }

    private fun saveRedeemToFirestore(proofUrl: String, post: Posts, progressDialog: AlertDialog) {
        val user = auth.currentUser ?: return
        val postId = currentDocId ?: ""

        val redeemData = hashMapOf(
            "claimerUid" to user.uid,
            "claimerEmail" to (user.email ?: "No Email"),
            "itemName" to post.itemName,
            "proofImageURL" to proofUrl,
            "postId" to postId,
            "originalItemURL" to post.imageURL,
            "status" to "pending",
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("redeems")
            .add(redeemData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Request Submitted!", Toast.LENGTH_SHORT).show()
                resetUI()
                (activity as? MainHome)?.binding?.pager?.currentItem = 0
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to save request", Toast.LENGTH_SHORT).show()
            }
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

    private fun resetUI() {
        HomeFragment.selectedImageUrl = null
        currentDocId = null
        imageUri = null

        binding.apply {
            IVPlaceholder.visibility = View.VISIBLE
            TVPlaceholder.visibility = View.VISIBLE
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
            TVItemName.text = ""
            TVDescription.text = ""
            IVItemImage.setImageDrawable(null)
        }
    }

    override fun onPause() {
        super.onPause()
        resetUI()
    }

    override fun onResume() {
        super.onResume()
        loadPostData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}