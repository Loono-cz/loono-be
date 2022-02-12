package cz.loono.backend.api.service

import cz.loono.backend.api.BasicUser

fun interface JwtAuthService {

    /**
     * Decode and verify the provided JWT. Returns [VerificationResult.Success] _if and only if_ the token is valid.
     */
    fun verifyToken(jwt: String): VerificationResult

    sealed class VerificationResult {
        data class Success(val basicUser: BasicUser) : VerificationResult()
        data class Error(val reason: String) : VerificationResult()
    }
}
