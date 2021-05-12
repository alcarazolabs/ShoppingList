package com.yonder.addtolist.presentation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.yonder.addtolist.R
import com.yonder.addtolist.databinding.ActivityMainBinding
import com.yonder.addtolist.extensions.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private var currentNavController: LiveData<NavController>? = null
  private val navGraphIds = listOf(R.navigation.list, R.navigation.settings)

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    if (savedInstanceState == null) {
      setupToolbar()
      setupBottomNavigationBar()
    }
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    setupToolbar()
    setupBottomNavigationBar()
  }

  private fun setupBottomNavigationBar() {
    val controller = binding.bottomNav.setupWithNavController(
      navGraphIds = navGraphIds,
      fragmentManager = supportFragmentManager,
      containerId = R.id.nav_host_container,
      intent = intent
    )

    controller.observe(this, { navController ->
      setupActionBarWithNavController(navController)
      navController.addOnDestinationChangedListener { _, destination, _ ->
        val showButton = showUpButton(destination.id)
        setUpButtonVisibility(showButton)
      }
    })

    currentNavController = controller
  }

  private fun setupToolbar() {
    setSupportActionBar(binding.toolbar)
  }

  private fun showUpButton(id: Int): Boolean {
    return id != R.id.splashScreen &&
        id != R.id.loginScreen &&
        id != R.id.settingsScreen
  }

  private fun setUpButtonVisibility(isVisible: Boolean) {
    supportActionBar?.setDisplayShowHomeEnabled(isVisible)
    supportActionBar?.setDisplayHomeAsUpEnabled(isVisible)
  }

  override fun onSupportNavigateUp(): Boolean {
    return currentNavController?.value?.navigateUp() ?: false
  }
}