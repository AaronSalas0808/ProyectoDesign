package com.example.proyecto

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.proyecto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        setupDrawer()
    }

    private fun setupDrawer() {
        val drawer = binding.drawerLayout

        // Deshabilita el gesto de swipe para abrir (solo se abre con el botón)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Blur progresivo mientras se abre el drawer (API 31+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val blur = slideOffset * 18f
                    binding.container.setRenderEffect(
                        RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.CLAMP)
                    )
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    binding.container.setRenderEffect(null)
                }
            }
        })

        // Scrim semi-transparente para APIs < 31 (sin blur nativo)
        drawer.setScrimColor(0x55000000.toInt())

        // Cierra el drawer al pulsar X dentro del menú
        val btnClose = drawer.findViewById<View>(R.id.btnCloseDrawer)
        btnClose?.setOnClickListener { drawer.closeDrawers() }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
    }
}
