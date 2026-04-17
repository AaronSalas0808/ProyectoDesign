package com.example.proyecto

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.proyecto.databinding.ActivityMainBinding
import com.example.proyecto.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var tvDrawerName: TextView? = null
    private var tvDrawerHandle: TextView? = null
    private var itemLogOut: View? = null
    private var btnCloseDrawer: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        setupDrawer()
        loadDrawerUser()
    }

    override fun onResume() {
        super.onResume()
        loadDrawerUser()
    }

    private fun setupDrawer() {
        val drawer = binding.drawerLayout

        // Referencias a vistas del burger menu
        tvDrawerName = drawer.findViewById(R.id.tvDrawerName)
        tvDrawerHandle = drawer.findViewById(R.id.tvDrawerHandle)
        itemLogOut = drawer.findViewById(R.id.itemLogOut)
        btnCloseDrawer = drawer.findViewById(R.id.btnCloseDrawer)

        // Deshabilita swipe para abrir
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
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

        // Fondo oscuro mientras abre
        drawer.setScrimColor(0x55000000.toInt())

        // Cerrar con la X
        btnCloseDrawer?.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
        }

        // Logout desde el menú
        itemLogOut?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ -> logoutUser() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun loadDrawerUser() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            goToLogin()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name").orEmpty()
                val username = document.getString("username").orEmpty()

                val displayName = when {
                    name.isNotBlank() -> name
                    !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                    !currentUser.email.isNullOrBlank() -> currentUser.email!!
                    else -> "Usuario"
                }

                val displayHandle = when {
                    username.isNotBlank() -> "@$username"
                    !currentUser.email.isNullOrBlank() -> "@${currentUser.email!!.substringBefore("@")}"
                    else -> "@usuario"
                }

                tvDrawerName?.text = displayName
                tvDrawerHandle?.text = displayHandle
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "No se pudo cargar el usuario del menú: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                val fallbackName = when {
                    !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                    !currentUser.email.isNullOrBlank() -> currentUser.email!!
                    else -> "Usuario"
                }

                val fallbackHandle = when {
                    !currentUser.email.isNullOrBlank() -> "@${currentUser.email!!.substringBefore("@")}"
                    else -> "@usuario"
                }

                tvDrawerName?.text = fallbackName
                tvDrawerHandle?.text = fallbackHandle
            }
    }

    fun openDrawer() {
        loadDrawerUser()
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    fun refreshDrawerUser() {
        loadDrawerUser()
    }

    fun logoutUser() {
        auth.signOut()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}