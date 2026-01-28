package activities.lostandfound.studentview

import activities.lostandfound.extras.BlankFragment
import activities.lostandfound.fragments.student.NewPostFragment
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import activities.lostandfound.fragments.student.EditPostFragment
import activities.lostandfound.login.Login
import android.content.Intent
import android.widget.TextView
import com.example.lostandfound.HomeFragment
import com.example.lostandfound.PostFragment
import com.example.lostandfound.R
import com.example.lostandfound.RedeemFragment
import com.example.lostandfound.databinding.ActivityMainHomeBinding
import com.example.lostandfoundsystem.extras.FragmentAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.tapadoo.alerter.Alerter

class MainHome : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainHomeBinding
        private set
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Handle system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Setup ViewPager + Bottom Navigation ---
        val fragments = listOf<Fragment>(
            HomeFragment(),
            RedeemFragment(),
            PostFragment(),
            BlankFragment(),
            NewPostFragment(),
            EditPostFragment()
        )

        val adapter = FragmentAdapter(fragments, supportFragmentManager, lifecycle)
        binding.pager.adapter = adapter

        val botnavbar = findViewById<ChipNavigationBar>(R.id.Menubar)
        botnavbar.setItemSelected(R.id.home, true)
        binding.pager.currentItem = 0


        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if(binding.pager.currentItem == 3){
                    binding.pager.currentItem = 2
                }

                if(binding.pager.currentItem == 2){
                    binding.pager.isUserInputEnabled = true
                }

                if(binding.pager.currentItem == 4){
                    binding.pager.isUserInputEnabled = false
                    botnavbar.setItemSelected(R.id.posts, false)
                }

                if(binding.pager.currentItem == 5){
                    binding.pager.isUserInputEnabled = false
                    botnavbar.setItemSelected(R.id.posts, false)
                }


                when (position) {
                    0 -> botnavbar.setItemSelected(R.id.home, true)
                    1 -> botnavbar.setItemSelected(R.id.redeem, true)
                    2 -> botnavbar.setItemSelected(R.id.posts, true)
                }

            }
        })

        botnavbar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.home -> binding.pager.currentItem = 0
                R.id.redeem -> binding.pager.currentItem = 1
                R.id.posts -> binding.pager.currentItem = 2
            }
        }

        // --- Setup Drawer Navigation ---
        drawerLayout = binding.drawerLayout
        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)
        updateNavHeader()

        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish()
            }
        }

        // Toolbar toggle (hamburger menu)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )


        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        listenForApprovedPosts()

    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_change_password -> Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_change_username -> Toast.makeText(this, "Change Username clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_about -> Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_logout -> FirebaseAuth.getInstance().signOut().also {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
                finish()
            }

        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }


    private fun listenForApprovedPosts() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("posts")
            .whereEqualTo("ownerId", userId)
            .whereEqualTo("approved", true)
            .whereEqualTo("notified", false)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                if (snapshots != null && !snapshots.isEmpty) {
                    for (docChange in snapshots.documentChanges) {
                        // Only trigger for new matches (Added) or when a post becomes approved (Modified)
                        if (docChange.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                            docChange.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {

                            val doc = docChange.document
                            val postTitle = doc.getString("itemName") ?: "Post"
                            val postId = doc.id

                            // Show the Alerter
                            Alerter.create(this)
                                .setTitle("Approval Notification")
                                .setText("Your post '$postTitle' has been approved!")
                                .setIcon(com.tapadoo.alerter.R.drawable.alerter_ic_notifications)
                                .setBackgroundColorRes(R.color.text_color)
                                .setDuration(4000)
                                .enableSwipeToDismiss()
                                .setOnClickListener {
                                    binding.pager.currentItem = 2
                                }
                                .show()

                            markPostAsNotified(postId)
                        }
                    }
                }
            }
    }



    private fun updateNavHeader() {
        val navigationView: NavigationView = binding.navView
        // Access the header layout (usually at index 0)
        val headerView = navigationView.getHeaderView(0)

        // Find the TextViews inside that headerView
        val tvUsername = headerView.findViewById<TextView>(R.id.TV_Username)
        val tvEmail = headerView.findViewById<TextView>(R.id.TV_Email)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            // Replace "Users" with your actual Firestore collection name
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") // replace with your field key
                        val email = document.getString("email")       // replace with your field key

                        tvUsername.text = username ?: "No Username"
                        tvEmail.text = email ?: "No Email"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun markPostAsNotified(postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .update("notified", true)
            .addOnFailureListener {
            }
    }




}
