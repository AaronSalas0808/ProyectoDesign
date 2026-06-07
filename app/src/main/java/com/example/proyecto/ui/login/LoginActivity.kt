package com.example.proyecto.ui.login

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.network.LoginRequestDto
import com.example.proyecto.network.RetrofitClient
import com.example.proyecto.network.UserDto
import com.example.proyecto.session.SessionManager
import com.example.proyecto.ui.register.RegisterActivity
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvCreateAccount: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)

        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvCreateAccount = findViewById(R.id.tvCreateAccount)

        setupFocusScroll(etUsuario, etPassword)

        btnLogin.setOnClickListener {
            validateAndLogin()
        }

        tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        val email = etUsuario.text.toString().trim()
        val password = etPassword.text.toString()

        when {
            email.isEmpty() -> {
                etUsuario.error = "Email is required"
                etUsuario.requestFocus()
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etUsuario.error = "Enter a valid email address"
                etUsuario.requestFocus()
                return
            }

            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return
            }

            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return
            }
        }

        loginUser(email, password)
    }

    private fun loginUser(email: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Signing in..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.loginUser(
                    LoginRequestDto(
                        email = email,
                        password = password
                    )
                )

                if (response.isSuccessful) {
                    val rawBody = response.body()?.string()
                    val user = parseUserFromResponse(rawBody)

                    if (user != null) {
                        sessionManager.saveUser(user)
                    } else {
                        sessionManager.saveUserManually(email = email)
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome back",
                        Toast.LENGTH_SHORT
                    ).show()

                    goToMain()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = extractErrorMessage(errorBody)

                    Toast.makeText(
                        this@LoginActivity,
                        message.ifBlank { "Login failed. Please try again." },
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnLogin.isEnabled = true
                btnLogin.text = "Sign In →"
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

    private fun parseUserFromResponse(rawBody: String?): UserDto? {
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
                name = userJson.optString("name", ""),
                email = userJson.optString("email", ""),
                university = userJson.optString("university", "")
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
}