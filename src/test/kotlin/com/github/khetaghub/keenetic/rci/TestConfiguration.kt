package com.github.khetaghub.keenetic.rci

import com.github.khetaghub.keenetic.rci.api.KeeneticApi
import com.github.khetaghub.keenetic.rci.parser.ResponseParser
import com.github.khetaghub.keenetic.rci.transport.http.HttpTransport
import com.github.khetaghub.keenetic.rci.transport.ssh.SshTransport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.provider.Arguments

object TestConfiguration {

    private val cfg = TestConfigLoader.load()
    private val parser = ResponseParser()

    val httpApi: KeeneticApi? by lazy {
        if (!cfg.keeneticRci.transport.http.enabled) return@lazy null

        val transport = HttpTransport.builder()
            .baseUrl(cfg.keeneticRci.transport.http.baseUrl)
            .credentials(
                username = cfg.keeneticRci.transport.http.username,
                password = cfg.keeneticRci.transport.http.password,
            )
            .build()

        KeeneticApi.create(transport, parser)
    }

    val sshApi: KeeneticApi? by lazy {
        if (!cfg.keeneticRci.transport.ssh.enabled) return@lazy null

        val transport = SshTransport.builder()
            .host(cfg.keeneticRci.transport.ssh.host)
            .port(cfg.keeneticRci.transport.ssh.port)
            .credentials(
                username = cfg.keeneticRci.transport.ssh.username,
                password = cfg.keeneticRci.transport.ssh.password,
            )
            .build()

        KeeneticApi.create(transport, parser)
    }

    @JvmStatic
    fun apis(): List<Arguments> = buildList {
        httpApi?.let { add(Arguments.of("HTTP", it)) }
        sshApi?.let { add(Arguments.of("SSH", it)) }
    }

    fun checkSame(request: (api: KeeneticApi) -> Any) {
        val results = apis()
            .map { arguments ->
                val args = arguments.get()
                val name = args[0] as String
                val api = args[1] as KeeneticApi

                name to assertDoesNotThrow {
                    request(api)
                }
            }
            .toList()
            .toMap()

        results.entries
            .zipWithNext()
            .forEach { (left, right) ->
                assertThat(left.value)
                    .isEqualTo(right.value)
                    .describedAs("Interfaces mismatch: ${left.key} vs ${right.key}")
            }
    }

}
