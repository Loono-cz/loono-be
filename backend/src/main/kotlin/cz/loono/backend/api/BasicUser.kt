package cz.loono.backend.api

import java.net.URL

/**
 * The basic user information provided by Firebase. This information is contained within the Firebase ID token itself.
 */
data class BasicUser(
    val uid: String,
    val email: String,
    val name: String,
    val photoUrl: URL?
)
