package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.command.GetVersionCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class RawCommandTest {

    private val command = GetVersionCommand()

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun executeRaw_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        val rawCommand = rawCommandExtractor(transport)
        val response = api.executeRaw(rawCommand)
        assertThat(response).isNotBlank()
    }

    private fun rawCommandExtractor(transport: String) = when (transport) {
        "HTTP" -> command.httpRequestBody
        "SSH" -> command.cliCommand.command
        else -> error("Unknown transport")
    }

}
