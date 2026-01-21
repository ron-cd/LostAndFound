package activities.lostandfound.studentview

import activities.lostandfound.login.Login
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.addCallback
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)


        // ✅ Custom ImageView menu button
        val menuButton = findViewById<ImageView>(R.id.IV_Menu)
        menuButton.setOnClickListener {
            Toast.makeText(this, "placeholder (on the making)", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }


        // --- ViewPager + Bottom Nav setup ---
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
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_change_password -> Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_change_username -> Toast.makeText(this, "Change Username clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_about -> Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_logout -> Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
        }
        return true
    }
}
