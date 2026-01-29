package activities.lostandfound.student_filters

import activities.lostandfound.extras.ActivePostsAdapter
import activities.lostandfound.extras.Posts
import activities.lostandfound.studentview.MainHome
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for displaying and filtering posts specifically in the 'Documents' category.
 */
class Documents : AppCompatActivity() {

    private lateinit var emptyStateText: TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postArrayList: ArrayList<Posts>
    private lateinit var myAdapter: ActivePostsAdapter
    private lateinit var searchField: EditText
    private lateinit var returnBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_documents)

        setupWindowInsets()
        initializeUI()
        setupSearchLogic()
        setupClickListeners()

        // Fetch data from Firestore
        getPostData()
    }

    /**
     * Handles system bar padding and sets fullscreen flags.
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
            insets
        }
    }

    /**
     * Initializes UI components and sets up the RecyclerView.
     */
    private fun initializeUI() {
        postsRecyclerView = findViewById(R.id.RV_LatestPosts)
        emptyStateText = findViewById(R.id.TV_EmptyState)
        searchField = findViewById(R.id.ET_SearchElectronic)
        returnBtn = findViewById(R.id.IV_returnBtn)

        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.setHasFixedSize(true)

        postArrayList = arrayListOf()
        myAdapter = ActivePostsAdapter(postArrayList) { }
        postsRecyclerView.adapter = myAdapter
    }

    /**
     * Sets up the TextWatcher for the search field.
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
     * Configures click listeners for navigation.
     */
    private fun setupClickListeners() {
        returnBtn.setOnClickListener {
            val intent = Intent(this, MainHome::class.java)
            startActivity(intent)
            finish() // Optional: prevents stacking filter activities
        }
    }

    /**
     * Local filtering logic based on user input.
     */
    fun searchList(text: String) {
        val filteredList = if (text.isBlank()) {
            postArrayList
        } else {
            postArrayList.filter { dataClass ->
                dataClass.itemName.contains(text, ignoreCase = true)
            }
        }
        myAdapter.searchDataList(ArrayList(filteredList))
    }

    /**
     * Listens for real-time Firestore updates specifically for approved 'Documents' posts.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun getPostData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("approved", true)
            .whereEqualTo("category", "Documents")
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

    override fun onRestart() {
        super.onRestart()
        getPostData()
    }

    override fun onResume() {
        super.onResume()
        getPostData()
    }
}