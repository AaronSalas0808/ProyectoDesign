package com.example.proyecto.ui.register

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.proyecto.R
import com.example.proyecto.model.UserProfile
import com.example.proyecto.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUser: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var ivProfilePhoto: ImageView

    private var capturedBitmap: Bitmap? = null

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                ivProfilePhoto.setPadding(0, 0, 0, 0)
                ivProfilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                ivProfilePhoto.setImageURI(uri)
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePicturePreviewLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                ivProfilePhoto.setPadding(0, 0, 0, 0)
                ivProfilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                ivProfilePhoto.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "No se tomó ninguna foto", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Sin permiso de cámara. Puedes usar la galería.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etUser = findViewById(R.id.etUser)
        etPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)

        setupFocusScroll(etName, etUser, etEmail, etPassword)

        ivProfilePhoto.setOnClickListener {
            showPhotoOptions()
        }

        btnRegister.setOnClickListener { validateAndRegister() }
        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener { finish() }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Elegir de galería", "Usar cámara")

        AlertDialog.Builder(this)
            .setTitle("Foto de perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> checkCameraPermissionAndOpen()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openGallery() {
        pickPhotoLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        takePicturePreviewLauncher.launch(null)
    }

    private fun validateAndRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val username = etUser.text.toString().trim()
        val password = etPassword.text.toString()

        if (name.isEmpty()) { etName.error = "Ingresa tu nombre"; etName.requestFocus(); return }
        if (email.isEmpty()) { etEmail.error = "Ingresa tu correo"; etEmail.requestFocus(); return }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.error = "Correo inválido"; etEmail.requestFocus(); return }
        if (username.isEmpty()) { etUser.error = "Ingresa tu nombre de usuario"; etUser.requestFocus(); return }
        if (password.isEmpty()) { etPassword.error = "Ingresa tu contraseña"; etPassword.requestFocus(); return }

        val pwdError = validatePassword(password)
        if (pwdError != null) {
            etPassword.error = pwdError
            etPassword.requestFocus()
            return
        }

        btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { authTask ->
                if (!authTask.isSuccessful) {
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "No se pudo registrar: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { docs ->
                        if (!docs.isEmpty) {
                            auth.currentUser?.delete()
                            btnRegister.isEnabled = true
                            etUser.error = "Este nombre de usuario ya está en uso"
                            etUser.requestFocus()
                        } else {
                            saveProfile(uid, name, email, username)
                        }
                    }
                    .addOnFailureListener {
                        saveProfile(uid, name, email, username)
                    }
            }
    }

    private fun saveProfile(uid: String, name: String, email: String, username: String) {
        val profile = UserProfile(uid = uid, name = name, email = email, username = username)
        db.collection("users").document(uid).set(profile)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                btnRegister.isEnabled = true
                Toast.makeText(this, "Cuenta creada, pero no se guardó el perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validatePassword(password: String): String? = when {
        password.length < 6 -> "Mínimo 6 caracteres"
        password.length > 12 -> "Máximo 12 caracteres"
        !password.any { it.isUpperCase() } -> "Debe tener al menos una mayúscula"
        !password.any { it.isLowerCase() } -> "Debe tener al menos una minúscula"
        !password.any { it.isDigit() } -> "Debe tener al menos un número"
        !password.all { it.isLetterOrDigit() } -> "No se permiten caracteres especiales"
        else -> null
    }

    private fun setupFocusScroll(vararg views: View) {
        val scrollView = findViewById<ScrollView>(R.id.registerScrollView)
        val focusListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                scrollView.postDelayed({
                    val rect = Rect()
                    view.getDrawingRect(rect)
                    scrollView.offsetDescendantRectToMyCoords(view, rect)
                    val extraTop = (24 * resources.displayMetrics.density).toInt()
                    scrollView.smoothScrollTo(0, (rect.top - extraTop).coerceAtLeast(0))
                }, 180)
            }
        }
        views.forEach { it.onFocusChangeListener = focusListener }
    }
}