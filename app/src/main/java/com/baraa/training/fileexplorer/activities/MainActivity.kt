package com.baraa.training.fileexplorer.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.baraa.training.fileexplorer.R
import com.baraa.training.fileexplorer.databinding.ActivityMainBinding
import com.baraa.training.fileexplorer.fragments.CardFragment
import com.baraa.training.fileexplorer.fragments.HomeFragment
import com.baraa.training.fileexplorer.fragments.InternalFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        setSupportActionBar(binding.toolbar)
        binding.navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.Open_Drawer
            , R.string.Close_Drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()

        binding.navView.setCheckedItem(R.id.homeFragment)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                supportFragmentManager.popBackStackImmediate()
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START)
                else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(callback)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.homeFragment -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, HomeFragment())
                    .commit()
            }
            R.id.internalFragment -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, InternalFragment())
                    .commit()
            }
            R.id.cardFragment -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, CardFragment())
                    .commit()
            }
            R.id.about -> {
                val deleteDialog = AlertDialog.Builder(this)
                deleteDialog.setTitle(R.string.about)
                deleteDialog.setMessage("Hello, my name is Baraa Abu Al-rob and I'm CSE & Mobile Dev.\n\n" +
                        "My contacts:\nPhone: +972 566 040 930\nEmail: baraabualrub109@gmail.com")
                deleteDialog.setPositiveButton("Ok") { _, _ -> }
                deleteDialog.create()
                deleteDialog.show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}