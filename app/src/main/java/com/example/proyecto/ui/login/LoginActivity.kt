package com.example.proyecto.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.ui.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Rect
import android.view.View
import android.widget.ScrollView

class LoginActivity : AppCompatActivity() {
    private fun setupFocusScroll(vararg views: View) {
        val scrollView = findViewById<ScrollView>(R.id.loginScrollView)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)
        setupFocusScroll(etUsuario, etPassword)

        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    etUsuario.error = "Ingrese el correo"
                    etUsuario.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etUsuario.error = "Correo inválido"
                    etUsuario.requestFocus()
                }
                password.isEmpty() -> {
                    etPassword.error = "Ingrese la contraseña"
                    etPassword.requestFocus()
                }
                else -> {
                    loginUser(email, password)
                }
            }
        }

        tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Credenciales incorrectas: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}