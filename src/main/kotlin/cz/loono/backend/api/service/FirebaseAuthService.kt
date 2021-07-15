package cz.loono.backend.api.service

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import cz.loono.backend.api.dto.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.text.ParseException

@Service
class FirebaseAuthService {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun verifyUser(user: UserDTO, token: String): Boolean {

        if (FirebaseApp.getApps().size == 0) {
            FirebaseApp.initializeApp()
        }

        val decodedToken: FirebaseToken
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(parseToken(token))
        } catch (e: FirebaseAuthException) {
            logger.warn("Used expired token.")
            return false
        }

        if (user.uid == decodedToken.uid) {
            return true
        }
        return false
    }

    private fun parseToken(token: String): String {
        val parsedToken = token.split(" ")
        if (!parsedToken[0].equals("Bearer", ignoreCase = true) || parsedToken.size < 2) {
            throw ParseException("Invalid format of Bearer token.", 0)
        }
        return token.split(" ")[1]
    }
}
