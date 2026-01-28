package com.example.lostandfound

import activities.lostandfound.extras.Posts
import activities.lostandfound.extras.ActivePostsAdapter
import activities.lostandfound.student_filters.Bags
import activities.lostandfound.student_filters.Documents
import activities.lostandfound.student_filters.Electronics
import activities.lostandfound.student_filters.Others
import activities.lostandfound.student_filters.Personal
import activities.lostandfound.student_filters.Wearables
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
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var emptyStateText: TextView
    private lateinit var postsRecyclerView : RecyclerView
    private lateinit var postArrayList: ArrayList<Posts>
    private lateinit var myAdapter: ActivePostsAdapter
    private lateinit var searchField: EditText

    private lateinit var electronic: CardView
    private lateinit var personal: CardView
    private lateinit var bags: CardView
    private lateinit var wearables : CardView
    private lateinit var documents : CardView
    private lateinit var others : CardView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize the RecyclerView
        postsRecyclerView = view.findViewById(R.id.RV_LatestPosts)
        emptyStateText = view.findViewById(R.id.TV_EmptyState)
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsRecyclerView.setHasFixedSize(true)

        // 2. Setup the Data List and Adapter
        postArrayList = arrayListOf<Posts>()
        myAdapter = ActivePostsAdapter(postArrayList)
        postsRecyclerView.adapter = myAdapter

        // 3. Fetch from Firestore
        getPostData()

        searchField = view.findViewById(R.id.ET_Search)

        searchField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This triggers whenever the user types
                searchList(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        electronic = view.findViewById(R.id.CV_Electronics)
        personal = view.findViewById(R.id.CV_PersonalItems)
        bags = view.findViewById(R.id.CV_Bags)
        wearables = view.findViewById(R.id.CV_Wearables)
        documents = view.findViewById(R.id.CV_Documents)
        others = view.findViewById(R.id.CV_Others)

        electronic.setOnClickListener {
            val intent = Intent(requireContext(), Electronics::class.java)
            startActivity(intent)
        }

        personal.setOnClickListener {
            val intent = Intent(requireContext(), Personal::class.java)
            startActivity(intent)
        }

        bags.setOnClickListener {
            val intent = Intent(requireContext(), Bags::class.java)
            startActivity(intent)
        }

        wearables.setOnClickListener {
            val intent = Intent(requireContext(), Wearables::class.java)
            startActivity(intent)
        }

        documents.setOnClickListener {
            val intent = Intent(requireContext(), Documents::class.java)
            startActivity(intent)
        }

        others.setOnClickListener {
            val intent = Intent(requireContext(), Others::class.java)
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
    override fun onResume() {
        super.onResume()
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