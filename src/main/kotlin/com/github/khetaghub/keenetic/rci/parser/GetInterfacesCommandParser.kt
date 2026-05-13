package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.Interface
import com.github.khetaghub.keenetic.rci.command.GetInterfacesCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

class GetInterfacesCommandParser(
    private val objectMapper: ObjectMapper
) : Parser<List<Interface>> {

    override val commandClass = GetInterfacesCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<List<Interface>>,
        response: String
    ): List<Interface> {
        val root = objectMapper.readTree(response)

        val interfacesNode = root
            .firstOrNull()
            ?.path("show")
            ?.path("interface")
            ?: throw KeeneticRciException("Unable to parse interfaces")

        if (interfacesNode.isMissingNode || interfacesNode.isNull || !interfacesNode.isObject) {
            throw KeeneticRciException("Unable to parse interfaces")
        }

        return interfacesNode.fields().asSequence()
            .mapNotNull { (_, node) -> node.toInterfaceOrNull() }
            .sortedWith(compareBy<Interface> { it.index }.thenBy { it.id })
            .toList()
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<List<Interface>>,
        response: String
    ): List<Interface> {
        return response
            .splitToInterfaceBlocks()
            .asSequence()
            .mapNotNull { block -> block.toInterfaceOrNull() }
            .sortedWith(compareBy<Interface> { it.index }.thenBy { it.id })
            .toList()
    }

    private fun JsonNode.toInterfaceOrNull(): Interface? {
        val id = path("id").asText("")
        val index = path("index").takeIf { it.isInt }?.asInt()
        val interfaceName = path("interface-name").asText("")
        val typeAlias = path("type").asText("")
        val description = path("description").asText("")

        if (
            id.isBlank() ||
            index == null ||
            interfaceName.isBlank() ||
            typeAlias.isBlank() ||
            typeAlias == PORT_TYPE
        ) {
            return null
        }

        return Interface(
            id = id,
            index = index,
            name = interfaceName,
            type = typeAlias,
            description = description,
        )
    }

    private fun CliInterfaceBlock.toInterfaceOrNull(): Interface? {
        val id = field("id")
        val index = field("index")?.toIntOrNull()
        val interfaceName = field("interface-name")
        val typeAlias = field("type")
        val description = field("description").orEmpty()

        if (
            id.isNullOrBlank() ||
            index == null ||
            interfaceName.isNullOrBlank() ||
            typeAlias.isNullOrBlank() ||
            typeAlias == PORT_TYPE
        ) {
            return null
        }

        return Interface(
            id = id,
            index = index,
            name = interfaceName,
            type = typeAlias,
            description = description,
        )
    }

    private fun String.splitToInterfaceBlocks(): List<CliInterfaceBlock> {
        return LINE_SEPARATOR
            .split(this)
            .asSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .mapNotNull { rawBlock ->
                val firstLineEnd = rawBlock.indexOf('\n')
                val header = if (firstLineEnd == -1) rawBlock else rawBlock.substring(0, firstLineEnd)
                val body = if (firstLineEnd == -1) "" else rawBlock.substring(firstLineEnd + 1)

                val displayName = INTERFACE_HEADER
                    .matchEntire(header.trim())
                    ?.groupValues
                    ?.get(1)
                    ?: return@mapNotNull null

                CliInterfaceBlock(
                    displayName = displayName,
                    body = body,
                )
            }
            .toList()
    }

    private data class CliInterfaceBlock(
        val displayName: String,
        val body: String,
    ) {
        fun field(name: String): String? {
            val regex = Regex("""^\s*${Regex.escape(name)}:\s*(.*)$""")

            return body
                .lineSequence()
                .takeWhile { line -> !line.isNestedSectionStart() }
                .mapNotNull { line ->
                    regex.matchEntire(line)
                        ?.groupValues
                        ?.get(1)
                        ?.trim()
                }
                .firstOrNull()
        }

        private fun String.isNestedSectionStart(): Boolean {
            val trimmed = trim()

            if (trimmed.isEmpty()) {
                return false
            }

            return trimmed == "summary:" ||
                    trimmed == "ipv6:" ||
                    trimmed == "bridge:" ||
                    trimmed == "wireguard:" ||
                    trimmed == "link-group:" ||
                    trimmed.startsWith("port, name =") ||
                    trimmed.startsWith("role, ") ||
                    trimmed.startsWith("interface, ")
        }
    }

    private companion object {
        private const val PORT_TYPE = "Port"

        private val LINE_SEPARATOR = Regex("""(?m)(?=^Interface,\s+name\s*=\s*")""")
        private val INTERFACE_HEADER = Regex("""Interface,\s+name\s*=\s*"([^"]*)"""")
    }
}
