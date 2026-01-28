package activities.lostandfound.student_filters

import activities.lostandfound.extras.ActivePostsAdapter
import activities.lostandfound.extras.Posts
import activities.lostandfound.studentview.MainHome
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

class Bags : AppCompatActivity() {


    private lateinit var emptyStateText: TextView
    private lateinit var postsRecyclerView : RecyclerView
    private lateinit var postArrayList: ArrayList<Posts>
    private lateinit var myAdapter: ActivePostsAdapter
    private lateinit var searchField: EditText
    private lateinit var  returnBtn: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bags)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
            insets
        }

        // 1. Initialize the RecyclerView
        postsRecyclerView = findViewById(R.id.RV_LatestPosts)
        emptyStateText = findViewById(R.id.TV_EmptyState)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        postsRecyclerView.setHasFixedSize(true)

        // 2. Setup the Data List and Adapter
        postArrayList = arrayListOf<Posts>()
        myAdapter = ActivePostsAdapter(postArrayList)
        postsRecyclerView.adapter = myAdapter

        // 3. Fetch from Firestore
        getPostData()

        searchField = findViewById(R.id.ET_SearchElectronic)

        searchField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchList(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        returnBtn = findViewById(R.id.IV_returnBtn)

        returnBtn.setOnClickListener {
            val intent = Intent(this, MainHome::class.java)
            startActivity(intent)
        }


    }

    fun searchList(text: String) {
        val filteredList = if (text.isBlank()) {
            postArrayList
        } else {
            postArrayList.filter { dataClass ->
                dataClass.itemName?.contains(text, ignoreCase = true) == true
            }
        }
        myAdapter.searchDataList(ArrayList(filteredList))
    }


    private fun getPostData() {
        val db = FirebaseFirestore.getInstance()

        // .whereEqualTo filters the collection before it reaches your app
        db.collection("posts")
            .whereEqualTo("approved", true) // Ensure this field exists in your Firestore doc
            .whereEqualTo("category","Bags")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                postArrayList.clear()

                if (snapshot != null && !snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val post = document.toObject(Posts::class.java)
                        if (post != null) {
                            postArrayList.add(post)
                        }
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