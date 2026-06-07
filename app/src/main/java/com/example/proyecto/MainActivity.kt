package com.example.proyecto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.proyecto.databinding.ActivityMainBinding
import com.example.proyecto.network.RetrofitClient
import com.example.proyecto.network.UpdateFcmTokenRequestDto
import com.example.proyecto.notifications.NotificationSocketManager
import com.example.proyecto.session.SessionManager
import com.example.proyecto.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    private var tvDrawerName: TextView? = null
    private var tvDrawerHandle: TextView? = null
    private var itemLogOut: View? = null
    private var btnCloseDrawer: View? = null
    private var btnEditProfile: View? = null

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d("FCM_PERMISSION", "Permiso notificaciones concedido: $isGranted")

            if (isGranted) {
                registerFcmToken()
            } else {
                Log.e("FCM_PERMISSION", "El usuario no concedió permiso de notificaciones")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)

        setupDrawer()
        loadDrawerUser()

        requestNotificationPermissionIfNeeded()
        registerFcmToken()
        startNotificationSocket()
    }

    override fun onResume() {
        super.onResume()

        if (::sessionManager.isInitialized && sessionManager.isLoggedIn()) {
            loadDrawerUser()
            startNotificationSocket()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // No lo detenemos aquí para permitir que siga vivo mientras el proceso exista.
        // Se detiene al cerrar sesión.
    }

    private fun setupDrawer() {
        val drawer = binding.drawerLayout

        tvDrawerName = drawer.findViewById(R.id.tvDrawerName)
        tvDrawerHandle = drawer.findViewById(R.id.tvDrawerHandle)
        itemLogOut = drawer.findViewById(R.id.itemLogOut)
        btnCloseDrawer = drawer.findViewById(R.id.btnCloseDrawer)
        btnEditProfile = drawer.findViewById(R.id.btnEditProfile)

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val blur = slideOffset * 18f
                    binding.container.setRenderEffect(
                        RenderEffect.createBlurEffect(
                            blur,
                            blur,
                            Shader.TileMode.CLAMP
                        )
                    )
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    binding.container.setRenderEffect(null)
                }
            }
        })

        drawer.setScrimColor(0x55000000.toInt())

        btnCloseDrawer?.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
        }

        btnEditProfile?.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)

            try {
                findNavController(R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.navigation_edit_profile)
            } catch (_: Exception) {
            }
        }

        itemLogOut?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    logoutUser()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun loadDrawerUser() {
        val name = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()

        val displayName = when {
            name.isNotBlank() -> name
            email.isNotBlank() -> email
            else -> "Usuario"
        }

        val displayHandle = when {
            email.isNotBlank() -> email
            else -> "@usuario"
        }

        tvDrawerName?.text = displayName
        tvDrawerHandle?.text = displayHandle
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
        NotificationSocketManager.stop()
        sessionManager.logout()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                requestNotificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                Log.d("FCM_PERMISSION", "Permiso de notificaciones ya estaba concedido")
            }
        }
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM_TOKEN", "Token generado: $token")

                sessionManager.saveFcmToken(token)

                val userId = sessionManager.getUserId()

                Log.d("FCM_TOKEN", "UserId en sesión: $userId")

                if (userId.isNotBlank()) {
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.api.updateFcmToken(
                                userId = userId,
                                body = UpdateFcmTokenRequestDto(token = token)
                            )

                            Log.d(
                                "FCM_TOKEN",
                                "Token enviado al backend. Código: ${response.code()}"
                            )
                        } catch (e: Exception) {
                            Log.e(
                                "FCM_TOKEN",
                                "Error enviando token al backend: ${e.message}"
                            )
                        }
                    }
                } else {
                    Log.e("FCM_TOKEN", "No hay userId para enviar token")
                }
            }
            .addOnFailureListener { e ->
                Log.e(
                    "FCM_TOKEN",
                    "No se pudo obtener token FCM: ${e.message}"
                )
            }
    }

    private fun startNotificationSocket() {
        val userId = sessionManager.getUserId()

        if (userId.isNotBlank()) {
            NotificationSocketManager.start(
                context = this,
                userId = userId
            )
        } else {
            Log.e(
                "NOTIF_SOCKET",
                "No se inició socket: userId vacío"
            )
        }
    }
}