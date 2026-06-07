package com.example.proyecto.session

import android.content.Context
import com.example.proyecto.network.UserDto

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUser(user: UserDto?) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", user?.id ?: "")
            .putString("user_name", user?.name ?: "")
            .putString("user_email", user?.email ?: "")
            .putString("user_university", user?.university ?: "")
            .apply()
    }

    fun saveUserManually(
        id: String? = null,
        name: String? = null,
        email: String,
        university: String? = null
    ) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", id ?: "")
            .putString("user_name", name ?: "")
            .putString("user_email", email)
            .putString("user_university", university ?: "")
            .apply()
    }

    fun updateName(newName: String) {
        prefs.edit()
            .putString("user_name", newName)
            .apply()
    }

    fun saveProfilePhotoPath(path: String) {
        prefs.edit()
            .putString("profile_photo_path", path)
            .apply()
    }

    fun getProfilePhotoPath(): String {
        return prefs.getString("profile_photo_path", "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getUserId(): String {
        return prefs.getString("user_id", "") ?: ""
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "") ?: ""
    }

    fun getUserEmail(): String {
        return prefs.getString("user_email", "") ?: ""
    }

    fun getUserUniversity(): String {
        return prefs.getString("user_university", "") ?: ""
    }

    fun getDisplayName(): String {
        val name = getUserName()
        val email = getUserEmail()

        return when {
            name.isNotBlank() -> name
            email.isNotBlank() -> email
            else -> "Usuario"
        }
    }

    fun getDisplayEmail(): String {
        val email = getUserEmail()

        return when {
            email.isNotBlank() -> email
            else -> "correo no disponible"
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun saveFcmToken(token: String) {
        prefs.edit()
            .putString("fcm_token", token)
            .apply()
    }

    fun getFcmToken(): String {
        return prefs.getString("fcm_token", "") ?: ""
    }
}