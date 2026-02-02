package activities.lostandfound.fragment.admin

import activities.lostandfound.extras.ManagePostsAdapter
import activities.lostandfound.extras.Posts
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.databinding.FragmentManagePostsBinding
import com.google.firebase.firestore.FirebaseFirestore


class ManagePosts : Fragment() {

    private var _binding: FragmentManagePostsBinding? = null
    private val binding get() = _binding!!
    private var postList = ArrayList<Posts>()
    private lateinit var searchField: EditText
    private lateinit var myAdapter: ManagePostsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagePostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)

        setupRecyclerView()
        loadImagesFromFirestore()
        setupSearchLogic()

    }

    private fun setupRecyclerView() {
        binding.RVPosts.layoutManager = GridLayoutManager(requireContext(), 2)
    }


    private fun initializeViews(view: View) {
        searchField = view.findViewById(R.id.ET_Search3)

    }


    private fun setupSearchLogic() {

        searchField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // In your Activity or Fragment where the search bar is located:
    private fun filter(text: String) {
        val filteredList = ArrayList<Posts>()

        // Iterate through your full original list (postList)
        for (item in postList) {
            // Search by item name or any other searchable property
            if (item.itemName.lowercase().contains(text.lowercase())) {
                filteredList.add(item)
            }
        }

        // Update the adapter with the filtered list
        myAdapter.searchDataList(filteredList)
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun loadImagesFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("approved", true)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                if (snapshots != null && !snapshots.isEmpty) {
                    postList.clear()
                    for (doc in snapshots) {
                        // 2. Convert the entire document to your Posts class
                        val post = doc.toObject(Posts::class.java)
                        if (post != null) {
                            postList.add(post)
                        }
                    }

                    // 3. Initialize or update the adapter with the onClick listener
                    if (binding.RVPosts.adapter == null) {
                        myAdapter = ManagePostsAdapter(postList) { selectedPost ->

                        }
                        binding.RVPosts.adapter = myAdapter
                    } else {
                        myAdapter.notifyDataSetChanged()
                    }
                }
            }
    }


}