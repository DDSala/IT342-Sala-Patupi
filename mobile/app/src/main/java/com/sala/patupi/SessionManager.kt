package com.sala.patupi

import org.json.JSONObject

object SessionManager {
    // 1. Temporary security tokens stored in RAM
    var authToken: String? = null
    var userEmail: String? = null

    // 2. This is the exact variable LoginActivity is trying to find!
    var currentUserJson: JSONObject? = null

    // 3. Status checks
    fun isLoggedIn(): Boolean {
        return authToken != null || currentUserJson != null
    }

    // 4. Wipe everything on exit
    fun logout() {
        authToken = null
        userEmail = null
        currentUserJson = null
    }
}