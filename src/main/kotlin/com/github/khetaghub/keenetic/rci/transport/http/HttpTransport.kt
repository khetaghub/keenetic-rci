package com.github.khetaghub.keenetic.rci.transport.http

import com.github.khetaghub.keenetic.rci.api.KeeneticTransport
import com.github.khetaghub.keenetic.rci.command.HttpBatchCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import mu.KotlinLogging
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.CookieManager
import java.net.CookiePolicy
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class HttpTransport private constructor(
    baseUrl: String,
    private val username: String,
    private val password: String,
    private val httpClient: OkHttpClient,
) : KeeneticTransport {

    private val normalizedBaseUrl: String = normalizeBaseUrl(baseUrl)
    private val authUrl: String = "$normalizedBaseUrl/$AUTH_PATH"
    private val rciUrl: String = "$normalizedBaseUrl/$RCI_PATH"

    private val logger = KotlinLogging.logger { }

    init {
        if (!auth()) {
            throw KeeneticRciException("Authentication failed")
        }
    }

    override fun execute(command: RciCommand<*>): String {
        val httpCommand = command as? HttpBatchCommand<*>
            ?: throw KeeneticRciException("HttpTransport supports only HTTP commands")
        val response = execute(buildPostRequest("$rciUrl/", httpCommand.httpRequestBody))
        logger.debug { "command=${command.javaClass.simpleName} response=$response" }
        return response
    }

    private fun md5(input: String): String = digest("MD5", input)

    private fun sha256(input: String): String = digest("SHA-256", input)

    private fun auth(): Boolean {
        httpClient.newCall(buildAuthRequest()).execute().use { response ->
            return when (response.code) {
                200 -> true
                401 -> {
                    val realm = response.header("X-NDM-Realm") ?: return false
                    val challenge = response.header("X-NDM-Challenge") ?: return false

                    val stage1Hash = md5("$username:$realm:$password")
                    val stage2Hash = sha256(challenge + stage1Hash)
                    httpClient.newCall(buildAuthRequest(stage2Hash)).execute().use(::isSuccessfulAuth)
                }

                else -> false
            }
        }
    }

    private fun execute(request: Request, allowRetry: Boolean = true): String {
        httpClient.newCall(request).execute().use { response ->
            if (response.code == 401 && allowRetry) {
                if (!auth()) {
                    throw KeeneticRciException("Authentication failed")
                }
                return execute(request, allowRetry = false)
            }

            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw KeeneticRciException(
                    "Request failed: HTTP ${response.code}, body=$body"
                )
            }

            return body
        }
    }

    private fun buildAuthRequest(passwordHash: String? = null): Request {
        val builder = Request.Builder().url(authUrl)

        return if (passwordHash == null) {
            builder.get().build()
        } else {
            builder.post(authPayload(passwordHash).toJsonBody()).build()
        }
    }

    private fun buildPostRequest(url: String, json: String): Request =
        Request.Builder()
            .url(url)
            .post(json.toJsonBody())
            .build()

    private fun buildRciUrl(query: String): String {
        val normalizedQuery = query.trimStart('/')
        return "$rciUrl/$normalizedQuery"
    }

    private fun authPayload(passwordHash: String): String = """
        {
          "login": "$username",
          "password": "$passwordHash"
        }
    """.trimIndent()

    private fun String.toJsonBody() = toRequestBody(JSON_MEDIA_TYPE)

    private fun isSuccessfulAuth(response: Response): Boolean = response.code == 200

    private fun digest(algorithm: String, input: String): String =
        MessageDigest.getInstance(algorithm)
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }

    class Builder {

        private var baseUrl: String? = null
        private var username: String? = null
        private var password: String? = null
        private var httpClient: OkHttpClient? = null

        fun baseUrl(baseUrl: String) = apply {
            this.baseUrl = baseUrl
        }

        fun credentials(username: String, password: String) = apply {
            this.username = username
            this.password = password
        }

        fun httpClient(client: OkHttpClient) = apply {
            this.httpClient = client
        }

        fun build(): HttpTransport {
            val baseUrl = requireNotNull(baseUrl) { "baseUrl is required" }
            val username = requireNotNull(username) { "username is required" }
            val password = requireNotNull(password) { "password is required" }
            val httpClient = httpClient ?: OkHttpClient().newBuilder()
                .cookieJar(
                    JavaNetCookieJar(
                        CookieManager().apply {
                            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
                        }
                    )
                )
                .connectTimeout(5L, TimeUnit.SECONDS)
                .readTimeout(10L, TimeUnit.SECONDS)
                .writeTimeout(10L, TimeUnit.SECONDS)
                .build()

            return HttpTransport(
                baseUrl = baseUrl,
                username = username,
                password = password,
                httpClient = httpClient
            )
        }
    }

    companion object {
        private const val AUTH_PATH = "auth"
        private const val RCI_PATH = "rci"

        fun builder(): Builder = Builder()

        private val JSON_MEDIA_TYPE = "application/json".toMediaType()

        private fun normalizeBaseUrl(input: String): String {
            val trimmed = input.trim().trimEnd('/')

            return when {
                trimmed.startsWith("http://", ignoreCase = true) -> trimmed
                trimmed.startsWith("https://", ignoreCase = true) -> trimmed
                else -> "http://$trimmed"
            }
        }
    }
}
