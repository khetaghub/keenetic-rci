package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.ProviderDnsState
import com.github.khetaghub.keenetic.rci.command.GetProviderDnsCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType

class GetProviderDnsCommandParser(
    private val objectMapper: ObjectMapper,
) : Parser<ProviderDnsState> {

    override val commandClass = GetProviderDnsCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<ProviderDnsState>,
        response: String,
    ): ProviderDnsState {
        command as GetProviderDnsCommand

        val root = objectMapper.readTree(response)
        val interfaceNode = root
            .findObjectPayload("show", "sc", "interface")
            ?.path(command.interfaceName)
            ?.takeIf(JsonNode::isObject)
            ?: return ProviderDnsState(ipv4 = false, ipv6 = false)

        val ipv4 = interfaceNode.path("ip").booleanOrFalse("name-servers")
        val ipv6 = interfaceNode.path("ipv6").booleanOrFalse("name-servers")

        return ProviderDnsState(ipv4 = ipv4, ipv6 = ipv6)
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<ProviderDnsState>,
        response: String,
    ): ProviderDnsState {
        command as GetProviderDnsCommand

        val lines = response.interfaceConfigLines(command.interfaceName)
        val ipv4 = lines.any { it == "ip name-servers" } &&
                lines.none { it == "ip no name-servers" }
        val ipv6 = lines.any { it == "ipv6 name-servers" } &&
                lines.none { it == "ipv6 no name-servers" }

        return ProviderDnsState(ipv4 = ipv4, ipv6 = ipv6)
    }

    private fun JsonNode.findObjectPayload(vararg path: String): JsonNode? {
        if (isArray) {
            return firstNotNullOfOrNull { node -> node.findObjectPayload(*path) }
        }

        val payload = path.fold(this) { node, field -> node.path(field) }
        return payload.takeIf(JsonNode::isObject)
    }

    private fun JsonNode.booleanOrFalse(field: String): Boolean {
        val value = path(field)
        if (value.isBoolean) return value.asBoolean()
        if (value.isTextual) return value.asText().asBooleanTextOrNull() ?: false
        return false
    }

    private fun String.asBooleanTextOrNull(): Boolean? {
        return when (lowercase()) {
            "true", "yes", "on", "1" -> true
            "false", "no", "off", "0" -> false
            else -> null
        }
    }

    private fun String.interfaceConfigLines(interfaceName: String): List<String> {
        val result = mutableListOf<String>()
        var inInterface = false

        lineSequence().forEach { rawLine ->
            val line = rawLine.trim()

            when {
                line.startsWith("interface ") -> {
                    val currentName = line.removePrefix("interface ")
                        .trim()
                        .removeSurrounding("\"")
                    inInterface = currentName == interfaceName
                }

                inInterface && line == "!" -> {
                    inInterface = false
                }

                inInterface && line.isNotBlank() -> {
                    result += line
                }
            }
        }

        return result
    }
}
