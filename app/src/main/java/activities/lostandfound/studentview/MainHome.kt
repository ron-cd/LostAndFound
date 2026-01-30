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
import androidx.viewpager2.widget.ViewPager2
import activities.lostandfound.fragments.student.EditPostFragment
import activities.lostandfound.login.Login
import android.content.Intent
import android.widget.TextView
import activities.lostandfound.fragments.student.HomeFragment
import activities.lostandfound.fragments.student.PostFragment
import com.example.lostandfound.R
import activities.lostandfound.fragments.student.RedeemFragment
import com.example.lostandfound.databinding.ActivityMainHomeBinding
import activities.lostandfound.extras.FragmentAdapter
import android.app.AlertDialog
import android.view.View
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.tapadoo.alerter.Alerter

/**
 * The primary container activity for the student view.
 * Manages the Bottom Navigation (ViewPager2), Navigation Drawer, and Approval Notifications.
 */
class MainHome : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainHomeBinding
        private set
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupWindowInsets()
        setupViewPager()
        setupDrawerNavigation()

        // Listeners & Headers
        updateNavHeader()
        listenForApprovedPosts()
    }

    /**
     * Handles system bar padding for an edge-to-edge experience.
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()

            insets
        }
    }

    /**
     * Configures the ViewPager2 with fragments and synchronizes it with the ChipNavigationBar.
     */
    private fun setupViewPager() {
        val fragments = listOf(
            HomeFragment(),
            RedeemFragment(),
            PostFragment(),
            BlankFragment(),
            NewPostFragment(),
            EditPostFragment()
        )

        val adapter = FragmentAdapter(fragments, supportFragmentManager, lifecycle)
        binding.pager.adapter = adapter

        val bottomNavBar = findViewById<ChipNavigationBar>(R.id.Menubar)
        bottomNavBar.setItemSelected(R.id.home, true)
        binding.pager.currentItem = 0

        // Handle page change synchronization and UI constraints
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Redirect BlankFragment to PostFragment
                if (binding.pager.currentItem == 3) {
                    binding.pager.currentItem = 2
                }

                // Enable/Disable swipe based on target fragment
                binding.pager.isUserInputEnabled = when (binding.pager.currentItem) {
                    4, 5 -> false // Disable swipe for New/Edit forms
                    else -> true
                }

                // Unselect menu items if on form pages
                if (binding.pager.currentItem == 4 || binding.pager.currentItem == 5) {
                    bottomNavBar.setItemSelected(R.id.posts, false)
                }

                // Sync Bottom Bar
                when (position) {
                    0 -> bottomNavBar.setItemSelected(R.id.home, true)
                    1 -> bottomNavBar.setItemSelected(R.id.redeem, true)
                    2 -> bottomNavBar.setItemSelected(R.id.posts, true)
                }
            }
        })

        // Handle Bottom Bar Clicks
        bottomNavBar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.home -> binding.pager.currentItem = 0
                R.id.redeem -> binding.pager.currentItem = 1
                R.id.posts -> binding.pager.currentItem = 2
            }
        }
    }

    /**
     * Sets up the Navigation Drawer and Toolbar toggle.
     */
    private fun setupDrawerNavigation() {
        drawerLayout = binding.drawerLayout
        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        // Custom back press handling
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish()
            }
        }

        // Hamburger Menu Toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val drawable = toggle.drawerArrowDrawable
        drawable.barLength = 80f
        drawable.barThickness = 8f
        toggle.drawerArrowDrawable = drawable
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_change_password -> handleChangePassword()
            R.id.nav_change_username -> showChangeUsernameDialog()
            R.id.nav_about -> AlertDialog.Builder(this)
                    .setTitle("About Us")
                    .setMessage("Lost & Found App v1.0\nCreated for NU Clark Students.\n\nContact: delacruzat@students.nu-clark.edu.ph")
                    .setPositiveButton("Close", null)
                    .show()

            R.id.nav_logout -> handleLogout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleLogout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, Login::class.java))
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handleChangePassword(){
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email != null) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Monitors Firestore for post approvals and triggers the Alerter notification.
     */
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
                        if (docChange.type == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                            docChange.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {

                            val doc = docChange.document
                            val postTitle = doc.getString("itemName") ?: "Post"
                            val postId = doc.id

                            showApprovalAlert(postTitle)
                            markPostAsNotified(postId)
                        }
                    }
                }
            }
    }

    private fun showChangeUsernameDialog() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Change Username")
            .setView(editText)
            .setPositiveButton("Update") { _, _ ->
                val newName = editText.text.toString()
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null && newName.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .update("username", newName) // Update the specific field
                        .addOnSuccessListener {
                            Toast.makeText(this, "Username updated!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showApprovalAlert(postTitle: String) {
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
    }

    /**
     * Fetches the current user profile data for the Navigation Header.
     */
    private fun updateNavHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val tvUsername = headerView.findViewById<TextView>(R.id.TV_Username)
        val tvEmail = headerView.findViewById<TextView>(R.id.TV_Email)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Use addSnapshotListener instead of .get() for real-time updates
        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    // Handle the error silently or log it
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    // The UI will now change automatically as soon as the database changes
                    tvUsername.text = document.getString("username") ?: "No Username"
                    tvEmail.text = document.getString("email") ?: "No Email"
                }
            }
    }

    private fun markPostAsNotified(postId: String) {
        FirebaseFirestore.getInstance().collection("posts")
            .document(postId)
            .update("notified", true)
    }
}