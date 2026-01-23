package com.example.lostandfound

import activities.lostandfound.extras.Posts
import activities.lostandfound.extras.RecyclerViewAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var postsRecyclerView : RecyclerView
    private lateinit var postArrayList: ArrayList<Posts>
    private lateinit var myAdapter: RecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize the RecyclerView
        postsRecyclerView = view.findViewById(R.id.RV_LatestPosts)
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postsRecyclerView.setHasFixedSize(true)

        // 2. Setup the Data List and Adapter
        postArrayList = arrayListOf<Posts>()
        myAdapter = RecyclerViewAdapter(postArrayList)
        postsRecyclerView.adapter = myAdapter

        // 3. Fetch from Firestore
        getPostData()
    }

    private fun getPostData() {
        val db = FirebaseFirestore.getInstance()

        // Replace "posts" with the exact name of your collection in Firebase
        db.collection("posts").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                postArrayList.clear()
                for (document in snapshot.documents) {
                    val post = document.toObject(Posts::class.java)
                    if (post != null) {
                        postArrayList.add(post)
                    }
                }
                // Refresh the list on screen
                myAdapter.notifyDataSetChanged()
            }
        }
    }
}