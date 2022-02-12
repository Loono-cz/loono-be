package cz.loono.backend.api.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import cz.loono.backend.api.BasicUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL

@Service
class FirebaseAuthService : JwtAuthService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun verifyToken(jwt: String): JwtAuthService.VerificationResult {
        if (FirebaseApp.getApps().size == 0) {
            val firebaseOptions = loadFirebaseCredentials()
                ?: return JwtAuthService.VerificationResult.Error("Could not verify JWT.")
            FirebaseApp.initializeApp(firebaseOptions)
        }

        val decodedToken: FirebaseToken = try {
            FirebaseAuth.getInstance().verifyIdToken(jwt)
        } catch (e: FirebaseAuthException) {
            logger.warn("Firebase verification failed: ${e.authErrorCode.name}", e)
            return JwtAuthService.VerificationResult.Error("Could not verify JWT.")
        }

        val user = BasicUser(decodedToken.uid, decodedToken.email, decodedToken.name, URL(decodedToken.picture))
        return JwtAuthService.VerificationResult.Success(user)
    }

    fun deleteAccount(uid: String) {
        val results = FirebaseAuth.getInstance().deleteUsers(listOf(uid))
        if (results.successCount != 1 || results.failureCount > 0) {
            logger.error(
                "Deleting account in the Firebase has failed. " +
                    "Successfully deleted count ${results.successCount} " +
                    "and failed deletion count ${results.failureCount}."
            )
        }
    }

    private fun loadFirebaseCredentials(): FirebaseOptions? {
        val credentialsContent = System.getenv("GOOGLE_APP_CREDENTIALS_CONTENT")
        if (credentialsContent.isNullOrEmpty()) {
            logger.warn("GOOGLE_APP_CREDENTIALS_CONTENT ENV variable is not set.")
            return null
        }
        val stream = credentialsContent.byteInputStream()
        return FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(stream))
            .build()
    }
}
