package activities.lostandfound.fragments.student

import activities.lostandfound.extras.Posts
import activities.lostandfound.extras.ActivePostsAdapter
import activities.lostandfound.student_filters.*
import activities.lostandfound.studentview.MainHome
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment responsible for displaying the main home feed and category filters.
 */
class HomeFragment : Fragment() {

    // UI Components
    private lateinit var emptyStateText: TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var searchField: EditText

    // Data and Adapter
    private lateinit var postArrayList: ArrayList<Posts>
    private lateinit var myAdapter: ActivePostsAdapter

    // Category Cards
    private lateinit var electronic: CardView
    private lateinit var personal: CardView
    private lateinit var bags: CardView
    private lateinit var wearables: CardView
    private lateinit var documents: CardView
    private lateinit var others: CardView

    companion object{
        var selectedImageUrl: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerView()
        setupSearchLogic()
        setupCategoryClickListeners()

        // Fetch data from Firestore
        getPostData()
    }


    @SuppressLint("InflateParams")
    private fun showPostDetailDialog(post: Posts) {
        val dialog = android.app.Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_post_summary, null)

        // Link your Dialog UI elements
        val imageView = view.findViewById<ImageView>(R.id.popupImage)
        val titleView = view.findViewById<TextView>(R.id.popupTitle)
        val locationView = view.findViewById<TextView>(R.id.popupLocation)
        val dateView = view.findViewById<TextView>(R.id.popupDate)
        val statusView = view.findViewById<TextView>(R.id.TV_found)
        val btnSeeMore = view.findViewById<Button>(R.id.btnSeeMore)

        // Fill the popup with the clicked post's data
        titleView.text = post.itemName
        locationView.text = post.place
        dateView.text = post.date

        Glide.with(this)
            .load(post.imageURL)
            .centerCrop()
            .into(imageView)

        // Show/hide status
        if (post.found) {
            statusView.visibility = View.VISIBLE
        } else {
            statusView.visibility = View.GONE
        }

        // Transparent background for rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(view)

        // Resize the dialog
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.6).toInt(),
            (resources.displayMetrics.heightPixels * 0.5).toInt()
        )

        btnSeeMore.setOnClickListener {
            dialog.dismiss()
            selectedImageUrl = post.imageURL
            (activity as? MainHome)?.binding?.pager?.currentItem = 1
        }

        dialog.show()
    }




    /**
     * Finds and initializes all view references.
     */
    private fun initializeViews(view: View) {
        postsRecyclerView = view.findViewById(R.id.RV_LatestPosts)
        emptyStateText = view.findViewById(R.id.TV_EmptyState)
        searchField = view.findViewById(R.id.ET_Search)

        electronic = view.findViewById(R.id.CV_Electronics)
        personal = view.findViewById(R.id.CV_PersonalItems)
        bags = view.findViewById(R.id.CV_Bags)
        wearables = view.findViewById(R.id.CV_Wearables)
        documents = view.findViewById(R.id.CV_Documents)
        others = view.findViewById(R.id.CV_Others)
    }

    /**
     * Configures the RecyclerView and its adapter.
     */
    private fun setupRecyclerView() {
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsRecyclerView.setHasFixedSize(true)

        postArrayList = arrayListOf()
        myAdapter = ActivePostsAdapter(postArrayList) { selectedPost ->
            showPostDetailDialog(selectedPost)
        }

        postsRecyclerView.adapter = myAdapter
    }

    /**
     * Sets up the search bar with a TextWatcher for real-time filtering.
     */
    private fun setupSearchLogic() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchList(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Navigates to the corresponding activity when a category is selected.
     */
    private fun setupCategoryClickListeners() {
        electronic.setOnClickListener { startActivity(Intent(requireContext(), Electronics::class.java)) }
        personal.setOnClickListener { startActivity(Intent(requireContext(), Personal::class.java)) }
        bags.setOnClickListener { startActivity(Intent(requireContext(), Bags::class.java)) }
        wearables.setOnClickListener { startActivity(Intent(requireContext(), Wearables::class.java)) }
        documents.setOnClickListener { startActivity(Intent(requireContext(), Documents::class.java)) }
        others.setOnClickListener { startActivity(Intent(requireContext(), Others::class.java)) }
    }

    /**
     * Filters the post list based on the search query.
     */
    fun searchList(text: String) {
        val filteredList = if (text.isBlank()) {
            postArrayList
        } else {
            postArrayList.filter { post ->
                post.itemName.contains(text, ignoreCase = true)
            }
        }
        myAdapter.searchDataList(ArrayList(filteredList))
    }

    /**
     * Listens for real-time updates from Firestore for approved posts.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getPostData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("approved", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                postArrayList.clear()

                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val post = document.toObject(Posts::class.java)
                        post?.let { postArrayList.add(it) }
                    }
                    postsRecyclerView.visibility = View.VISIBLE
                    emptyStateText.visibility = View.GONE
                } else {
                    postsRecyclerView.visibility = View.GONE
                    emptyStateText.visibility = View.VISIBLE
                }

                myAdapter.notifyDataSetChanged()
            }
    }

    override fun onResume() {
        super.onResume()

        // Handle keyboard hiding and search field reset
        searchField.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchField.windowToken, 0)

        searchField.setText(".")
        searchField.postDelayed({
            searchField.text.clear()
            searchField.clearFocus()
        }, 10)
    }
}