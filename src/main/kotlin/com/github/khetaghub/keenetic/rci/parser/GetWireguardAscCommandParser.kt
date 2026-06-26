package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.command.GetWireguardAscCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType

class GetWireguardAscCommandParser(
    private val objectMapper: ObjectMapper,
) : Parser<Map<String, Map<String, String>>> {

    override val commandClass = GetWireguardAscCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<Map<String, Map<String, String>>>,
        response: String,
    ): Map<String, Map<String, String>> {
        val root = objectMapper.readTree(response)
        val interfacesNode = root
            .findObjectPayload("show", "sc", "interface")
            ?: return emptyMap()

        return interfacesNode.fields().asSequence()
            .mapNotNull { (name, interfaceNode) ->
                val asc = interfaceNode.path("wireguard").asc().ifEmpty { interfaceNode.asc() }
                if (asc.isEmpty()) null else name to asc
            }
            .toMap()
    }

    private fun JsonNode.findObjectPayload(vararg path: String): JsonNode? {
        if (isArray) {
            return firstNotNullOfOrNull { node -> node.findObjectPayload(*path) }
        }

        val payload = path.fold(this) { node, field -> node.path(field) }
        return payload.takeIf(JsonNode::isObject)
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<Map<String, Map<String, String>>>,
        response: String,
    ): Map<String, Map<String, String>> {
        val result = linkedMapOf<String, Map<String, String>>()
        var currentInterface: String? = null

        response.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isBlank() || line == "!" -> {
                    currentInterface = null
                }

                line.startsWith("interface ") -> {
                    currentInterface = line
                        .removePrefix("interface ")
                        .trim()
                        .removeSurrounding("\"")
                }

                line.startsWith("wireguard asc ") -> {
                    val interfaceName = currentInterface ?: return@forEach
                    val asc = line
                        .removePrefix("wireguard asc ")
                        .trim()
                        .toAscMap(ASC_PARAMETER_ORDER)

                    if (asc.isNotEmpty()) {
                        result[interfaceName] = asc
                    }
                }
            }
        }

        return result
    }

    private fun JsonNode.asc(): Map<String, String> {
        val ascNode = path("asc")
        if (ascNode.isObject) {
            return ascNode.fields().asSequence()
                .mapNotNull { (key, value) ->
                    val canonicalKey = ASC_PARAMETER_ORDER.firstOrNull { it.equals(key, ignoreCase = true) }
                        ?: return@mapNotNull null
                    canonicalKey to value.asText()
                }
                .toMap()
        }
        if (ascNode.isArray) {
            return ascNode.map { it.asText() }.toAscMap(ASC_PARAMETER_ORDER)
        }
        if (!ascNode.isMissingNode && !ascNode.isNull) {
            return ascNode.asText().toAscMap(ASC_PARAMETER_ORDER)
        }

        val result = linkedMapOf<String, String>()
        ASC_PARAMETER_ORDER.forEach { parameter ->
            val value = path(parameter)
            if (!value.isMissingNode && !value.isNull) {
                result[parameter] = value.asText()
            }
        }
        return result
    }

    private companion object {
        private val ASC_PARAMETER_ORDER = listOf(
            "Jc",
            "Jmin",
            "Jmax",
            "S1",
            "S2",
            "H1",
            "H2",
            "H3",
            "H4",
            "S3",
            "S4",
            "I1",
            "I2",
            "I3",
            "I4",
            "I5",
        )
    }
}
