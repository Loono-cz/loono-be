package cz.loono.backend.api.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.auth.UserRecord
import cz.loono.backend.api.BasicUser
import cz.loono.backend.db.model.UserAuxiliary
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

        if (decodedToken.email == null) {
            val error = "Firebase accounts without email are not permitted.\n" +
                "UID: ${decodedToken.uid}\n" +
                "Probable reason: Client application may have allowed a login method " +
                "which doesn't provide user email. Loono only permits login methods which provide user email."
            logger.error(error)

            return JwtAuthService.VerificationResult.MissingPrimaryEmail
        }

        if (decodedToken.name == null) {
            val error = "Firebase accounts without name are not permitted.\n" +
                "UID: ${decodedToken.uid}\n" +
                "Probable reason: Client application may have allowed a login method " +
                "which doesn't provide account name. Loono only permits login methods which provide user name."
            logger.error(error)

            return JwtAuthService.VerificationResult.MissingUserName
        }

        val user = BasicUser(decodedToken.uid, decodedToken.email, decodedToken.name, URL(decodedToken.picture))
        return JwtAuthService.VerificationResult.Success(user)
    }

    fun updateUser(uid: String, userAuxiliary: UserAuxiliary) {
        val request: UserRecord.UpdateRequest = UserRecord.UpdateRequest(uid)
        var change = false

        if (userAuxiliary.nickname != null) {
            request.setDisplayName(userAuxiliary.nickname)
            change = true
        }
        if (userAuxiliary.profileImageUrl != null) {
            request.setPhotoUrl(userAuxiliary.profileImageUrl)
            change = true
        }

        if (change) {
            FirebaseAuth.getInstance().updateUser(request)
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
