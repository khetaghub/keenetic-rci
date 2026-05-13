package com.github.khetaghub.keenetic.rci.transport

import com.github.khetaghub.keenetic.rci.api.KeeneticTransport
import com.github.khetaghub.keenetic.rci.command.HttpBatchCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciTransportAuthException
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciTransportException
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

    @Volatile
    private var authenticated = false

    override fun execute(command: RciCommand<*>): String {
        val httpCommand = command as? HttpBatchCommand<*>
            ?: throw KeeneticRciTransportException("HttpTransport supports only HTTP commands")
        val response = executeRequest(httpCommand.httpRequestBody)
        logger.debug { "command=${command.javaClass.simpleName} response=$response" }
        return response
    }

    override fun execute(rawCommand: String): String {
        val response = executeRequest(rawCommand)
        logger.debug { "command='$rawCommand' response=$response" }
        return response
    }

    private fun executeRequest(requestBody: String, allowRetry: Boolean = true): String {
        val request = buildPostRequest("$rciUrl/", requestBody)

        ensureAuthenticated()

        val response: String = httpClient.newCall(request).execute().use { response ->
            if (response.code == 401 && allowRetry) {
                auth()
                authenticated = true
                return executeRequest(requestBody, allowRetry = false)
            }

            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw KeeneticRciTransportException(
                    "Request failed: HTTP ${response.code}, body=$body"
                )
            }

            body
        }

        return response
    }

    private fun md5(input: String): String = digest("MD5", input)

    private fun sha256(input: String): String = digest("SHA-256", input)

    private fun ensureAuthenticated() {
        if (authenticated) return

        synchronized(this) {
            if (authenticated) return

            auth()

            authenticated = true
        }
    }

    private fun auth() {
        val code = httpClient.newCall(buildAuthRequest()).execute().use { response ->
            when (response.code) {
                200 -> 200
                401 -> challengeAuth(response)
                else -> response.code
            }
        }

        if (code == 401) {
            throw KeeneticRciTransportAuthException("HTTP transport authentication failed")
        }
        if (code != 200) {
            throw KeeneticRciTransportException("Request failed with HTTP code $code")
        }
    }

    private fun challengeAuth(response: Response): Int {
        val realm = response.header("X-NDM-Realm") ?: return 401
        val challenge = response.header("X-NDM-Challenge") ?: return 401

        val stage1Hash = md5("$username:$realm:$password")
        val stage2Hash = sha256(challenge + stage1Hash)

        val authResponse = httpClient.newCall(buildAuthRequest(stage2Hash))
            .execute()
        return authResponse.use { it.code }
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

    private fun authPayload(passwordHash: String): String =
        """{"login":"${username.toJsonString()}","password":"$passwordHash"}"""

    private fun String.toJsonBody() = toRequestBody(JSON_MEDIA_TYPE)

    private fun String.toJsonString(): String =
        JsonStringEncoder.getInstance().quoteAsString(this).concatToString()

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

        @JvmStatic
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
