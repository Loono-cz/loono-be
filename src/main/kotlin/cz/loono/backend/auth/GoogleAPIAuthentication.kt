package cz.loono.backend.auth

import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.json.gson.GsonFactory
import java.util.Collections

class GoogleAPIAuthentication {

    companion object {
        const val CLIENT_ID = "811307376278-pr4g0e46urvicdin06i80f2p2fr1u9j8.apps.googleusercontent.com"
    }

    fun verifyUser(accessToken: String): String {
        val verifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(
            UrlFetchTransport.getDefaultInstance(),
            GsonFactory.getDefaultInstance()
        )
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build()

        val idToken: GoogleIdToken = verifier.verify(accessToken)
        val payload: Payload = idToken.payload

        // Print user identifier
        val userId: String = payload.subject
        println("User ID: $userId")

        // Get profile information from payload
        val email: String = payload.email
        //        val emailVerified: Boolean = java.lang.Boolean.valueOf(payload.emailVerified)
        //        val name = payload["name"] as String
        //        val pictureUrl = payload["picture"] as String
        //        val locale = payload["locale"] as String
        //        val familyName = payload["family_name"] as String
        //        val givenName = payload["given_name"] as String
        //
        return email
    }
}
