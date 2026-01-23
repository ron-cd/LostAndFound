package activities.lostandfound.studentview

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
import com.example.lostandfound.HomeFragment
import com.example.lostandfound.PostFragment
import com.example.lostandfound.R
import com.example.lostandfound.RedeemFragment
import com.example.lostandfound.databinding.ActivityMainHomeBinding
import com.example.lostandfoundsystem.extras.FragmentAdapter
import com.google.android.material.navigation.NavigationView
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainHome : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainHomeBinding
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
            PostFragment()
        )
        val adapter = FragmentAdapter(fragments, supportFragmentManager, lifecycle)
        binding.pager.adapter = adapter

        val botnavbar = findViewById<ChipNavigationBar>(R.id.Menubar)
        botnavbar.setItemSelected(R.id.home, true)
        binding.pager.currentItem = 0

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
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

        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                // Finish the activity if drawer is closed
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

    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_change_password -> Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_change_username -> Toast.makeText(this, "Change Username clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_about -> Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_logout -> Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }


}
