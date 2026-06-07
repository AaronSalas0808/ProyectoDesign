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
import androidx.lifecycle.lifecycleScope
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.network.RegisterRequestDto
import com.example.proyecto.network.RetrofitClient
import com.example.proyecto.network.UserDto
import com.example.proyecto.session.SessionManager
import com.example.proyecto.ui.login.LoginActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUniversity: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var sessionManager: SessionManager

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
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etUniversity = findViewById(R.id.etUser)
        etPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)

        setupFocusScroll(etName, etUniversity, etEmail, etPassword)

        ivProfilePhoto.setOnClickListener {
            showPhotoOptions()
        }

        btnRegister.setOnClickListener {
            validateAndRegister()
        }

        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateAndRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val university = etUniversity.text.toString().trim()
        val password = etPassword.text.toString()

        when {
            name.isEmpty() -> {
                etName.error = "Full name is required"
                etName.requestFocus()
                return
            }

            name.length < 3 -> {
                etName.error = "Name must be at least 3 characters"
                etName.requestFocus()
                return
            }

            email.isEmpty() -> {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Enter a valid email address"
                etEmail.requestFocus()
                return
            }

            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return
            }

            password.length < 8 -> {
                etPassword.error = "Password must be at least 8 characters"
                etPassword.requestFocus()
                return
            }
        }

        registerUser(
            name = name,
            email = email,
            university = university,
            password = password
        )
    }

    private fun registerUser(
        name: String,
        email: String,
        university: String,
        password: String
    ) {
        btnRegister.isEnabled = false
        btnRegister.text = "Creating account..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.registerUser(
                    RegisterRequestDto(
                        name = name,
                        email = email,
                        university = university,
                        password = password
                    )
                )

                if (response.isSuccessful) {
                    val rawBody = response.body()?.string()

                    val user = parseUserFromResponse(
                        rawBody = rawBody,
                        fallbackName = name,
                        fallbackEmail = email,
                        fallbackUniversity = university
                    )

                    if (user != null) {
                        sessionManager.saveUser(user)
                    } else {
                        sessionManager.saveUserManually(
                            name = name,
                            email = email,
                            university = university
                        )
                    }

                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToMain()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = extractErrorMessage(errorBody)

                    Toast.makeText(
                        this@RegisterActivity,
                        message.ifBlank { "Sign up failed. Please try again." },
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnRegister.isEnabled = true
                btnRegister.text = "Create Account →"
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun parseUserFromResponse(
        rawBody: String?,
        fallbackName: String,
        fallbackEmail: String,
        fallbackUniversity: String
    ): UserDto? {
        return try {
            if (rawBody.isNullOrBlank()) return null

            val root = JSONObject(rawBody)

            val userJson = when {
                root.has("user") && !root.isNull("user") -> root.getJSONObject("user")

                root.has("data") && !root.isNull("data") -> {
                    val data = root.getJSONObject("data")
                    if (data.has("user") && !data.isNull("user")) {
                        data.getJSONObject("user")
                    } else {
                        data
                    }
                }

                else -> root
            }

            UserDto(
                id = when {
                    userJson.has("id") && !userJson.isNull("id") -> userJson.optString("id")
                    userJson.has("_id") && !userJson.isNull("_id") -> userJson.optString("_id")
                    userJson.has("uid") && !userJson.isNull("uid") -> userJson.optString("uid")
                    else -> null
                },
                name = userJson.optString("name", fallbackName),
                email = userJson.optString("email", fallbackEmail),
                university = userJson.optString("university", fallbackUniversity)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody.isNullOrBlank()) return ""

            val json = JSONObject(errorBody)

            when {
                json.has("message") -> json.optString("message")
                json.has("error") -> json.optString("error")
                else -> errorBody
            }
        } catch (e: Exception) {
            errorBody ?: ""
        }
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