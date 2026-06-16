package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.PlainDns
import com.github.khetaghub.keenetic.rci.command.GetPlainDnsCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

class GetPlainDnsCommandParser(
    private val objectMapper: ObjectMapper
) : Parser<List<PlainDns>> {

    override val commandClass = GetPlainDnsCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<List<PlainDns>>,
        response: String
    ): List<PlainDns> {
        return try {
            val root = objectMapper.readTree(response)

            root.path("server")
                .mapNotNull { node ->
                    val address = node.path("address").textOrNull()
                        ?: return@mapNotNull null

                    PlainDns(
                        address = address,
                        domain = node.path("domain").textOrNull(),
                        interfaceName = node.path("interface").textOrNull()
                    )
                }
        } catch (ex: Exception) {
            throw KeeneticRciException(
                "Failed to parse plain DNS response",
                ex
            )
        }
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<List<PlainDns>>,
        response: String
    ): List<PlainDns> {
        return try {
            parsePlainDnsCliOutput(response)
        } catch (ex: Exception) {
            throw KeeneticRciException(
                "Failed to parse plain DNS CLI output",
                ex
            )
        }
    }

    private fun parsePlainDnsCliOutput(response: String): List<PlainDns> {
        val servers = mutableListOf<PlainDns>()

        var address: String? = null
        var domain: String? = null
        var interfaceName: String? = null

        fun flushServer() {
            val currentAddress = address ?: return

            // The CLI prints one "server:" block per DNS server; a new block starts
            // only after the previous one has been collected.
            servers += PlainDns(
                address = currentAddress,
                domain = domain,
                interfaceName = interfaceName
            )

            address = null
            domain = null
            interfaceName = null
        }

        response
            .lineSequence()
            .map { it.trim() }
            .forEach { line ->
                when {
                    line == "server:" -> {
                        flushServer()
                    }

                    line.startsWith("address:") -> {
                        address = line.valueAfterColon()
                    }

                    line.startsWith("domain:") -> {
                        domain = line.valueAfterColon()
                    }

                    line.startsWith("interface:") -> {
                        interfaceName = line.valueAfterColon()
                    }
                }
            }

        flushServer()

        return servers
    }

    private fun JsonNode.textOrNull(): String? {
        if (isMissingNode || isNull) return null

        val value = asText()
        return value.takeIf { it.isNotBlank() }
    }

    private fun String.valueAfterColon(): String? {
        return substringAfter(':')
            .trim()
            .takeIf { it.isNotBlank() }
    }
}
