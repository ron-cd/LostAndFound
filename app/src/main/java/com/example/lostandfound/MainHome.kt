package com.example.lostandfound

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.lostandfound.databinding.ActivityMainHomeBinding
import com.example.lostandfoundsystem.FragmentAdapter

class MainHome : AppCompatActivity() {
    lateinit var binding: ActivityMainHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fragments = listOf<Fragment>(
            HomeFragment(),
            RedeemFragment(),
            PostFragment()
        )
        val adapter = FragmentAdapter(
            fragments,
            supportFragmentManager,
            lifecycle
        )
        val botnavbar = findViewById<com.ismaeldivita.chipnavigation.ChipNavigationBar>(R.id.Menubar)

        binding.pager.adapter = adapter

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> botnavbar.setItemSelected(R.id.home , true)
                    1 -> botnavbar.setItemSelected(R.id.redeem, true)
                    2 -> botnavbar.setItemSelected(R.id.posts, true)
                }
            }
        })

        // --- Keep pager in sync with nav bar clicks ---
        botnavbar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.home-> binding.pager.currentItem = 0
                R.id.redeem -> binding.pager.currentItem = 1
                R.id.posts -> binding.pager.currentItem = 2
            }
        }

        botnavbar.setItemSelected(R.id.home, true)
        binding.pager.currentItem = 0


    }
}