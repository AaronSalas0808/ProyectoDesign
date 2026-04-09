package com.example.proyecto.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.R
import com.example.proyecto.model.UserProfile
import com.example.proyecto.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Rect
import android.view.View
import android.widget.ScrollView

class RegisterActivity : AppCompatActivity() {

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
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etUser = findViewById<EditText>(R.id.etUser)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        setupFocusScroll(etName, etUser, etEmail, etPassword)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val username = etUser.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                name.isEmpty() -> {
                    etName.error = "Ingrese el nombre"
                    etName.requestFocus()
                }
                email.isEmpty() -> {
                    etEmail.error = "Ingrese el correo"
                    etEmail.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Correo inválido"
                    etEmail.requestFocus()
                }
                username.isEmpty() -> {
                    etUser.error = "Ingrese el usuario"
                    etUser.requestFocus()
                }
                password.isEmpty() -> {
                    etPassword.error = "Ingrese la contraseña"
                    etPassword.requestFocus()
                }
                password.length < 6 -> {
                    etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                    etPassword.requestFocus()
                }
                else -> {
                    registerUser(name, email, username, password)
                }
            }
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(name: String, email: String, username: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { authTask ->
                if (authTask.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: return@addOnCompleteListener

                    val profile = UserProfile(
                        uid = uid,
                        name = name,
                        email = email,
                        username = username
                    )

                    db.collection("users")
                        .document(uid)
                        .set(profile)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Cuenta creada, pero no se guardó el perfil: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo registrar: ${authTask.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}