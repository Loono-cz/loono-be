package cz.loono.backend.api.smartemailng

import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class EmailInterceptor(user: String?, password: String?) : Interceptor {
    companion object {
        val SMARTEMAILING_USER: String = System.getenv().getOrDefault("SMARTEMAILING_USER", "")
        val SMARTEMAILING_PSW: String = System.getenv().getOrDefault("SMARTEMAILING_PSW", "")
    }
    private val credentials: String

    init {
        credentials = basic(user!!, password!!)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val request: Request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", credentials).build()
        return chain.proceed(authenticatedRequest)
    }
}
