package activities.lostandfound.adminview

import activities.lostandfound.extras.FragmentAdapter
import activities.lostandfound.fragment.admin.AdminManageAccount
import activities.lostandfound.fragment.admin.ApproveAccount
import activities.lostandfound.fragment.admin.ManagePosts
import activities.lostandfound.login.Login
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityAdminhomeBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class AdminHome : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityAdminhomeBinding
        private set
    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminhomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        setupWindowInsets()
        setupViewPager()
        setupDrawerNavigation()

        updateAdminNavHeader()


    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()

            insets
        }
    }

    private fun setupViewPager() {
        val fragments = listOf(
            AdminManageAccount(),
            ApproveAccount(),
            ManagePosts()
        )

        val adapter = FragmentAdapter(fragments, supportFragmentManager, lifecycle)
        binding.pager.adapter = adapter

        val bottomNavBar = findViewById<ChipNavigationBar>(R.id.Menubar)
        bottomNavBar.setItemSelected(R.id.manage_accounts, true)
        binding.pager.currentItem = 0

        // Handle page change synchronization and UI constraints
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Sync Bottom Bar
                when (position) {
                    0 -> bottomNavBar.setItemSelected(R.id.manage_accounts, true)
                    1 -> bottomNavBar.setItemSelected(R.id.approve_acccunts, true)
                    2 -> bottomNavBar.setItemSelected(R.id.manage_posts, true)
                }
            }
        })

        // Handle Bottom Bar Clicks
        bottomNavBar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.manage_accounts -> binding.pager.currentItem = 0
                R.id.approve_acccunts -> binding.pager.currentItem = 1
                R.id.manage_posts -> binding.pager.currentItem = 2
            }
        }
    }

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

    private fun updateAdminNavHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val tvUsername = headerView.findViewById<TextView>(R.id.TV_Username)
        val tvEmail = headerView.findViewById<TextView>(R.id.TV_Email)

        tvEmail.text = "NUSdao@admin.nu-clark.edu.ph"
        tvUsername.text = "Admin"
    }
}
