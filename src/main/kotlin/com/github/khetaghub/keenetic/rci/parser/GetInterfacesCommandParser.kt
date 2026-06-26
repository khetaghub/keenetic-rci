package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.BaseInterface
import com.github.khetaghub.keenetic.rci.api.InternetProviderInterface
import com.github.khetaghub.keenetic.rci.api.WireguardInterface
import com.github.khetaghub.keenetic.rci.api.WireguardPeer
import com.github.khetaghub.keenetic.rci.command.GetInterfacesCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

class GetInterfacesCommandParser(
    private val objectMapper: ObjectMapper
) : Parser<List<BaseInterface>> {

    override val commandClass = GetInterfacesCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<List<BaseInterface>>,
        response: String
    ): List<BaseInterface> {
        val root = objectMapper.readTree(response)
        val detailsMode = (command as GetInterfacesCommand).detailed

        val interfacesNode = root
            .firstOrNull()
            ?.path("show")
            ?.path("interface")
            ?: throw KeeneticRciException("Unable to parse interfaces")

        if (interfacesNode.isMissingNode || interfacesNode.isNull || !interfacesNode.isObject) {
            throw KeeneticRciException("Unable to parse interfaces")
        }

        return interfacesNode.fields().asSequence()
            .mapNotNull { (_, node) -> node.toInterfaceOrNull(detailsMode) }
            .sortedWith(compareBy<BaseInterface> { it.index }.thenBy { it.id })
            .toList()
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<List<BaseInterface>>,
        response: String
    ): List<BaseInterface> {
        val detailsMode = (command as GetInterfacesCommand).detailed

        return response
            .splitToInterfaceBlocks()
            .asSequence()
            .mapNotNull { block -> block.toInterfaceOrNull(detailsMode) }
            .sortedWith(compareBy<BaseInterface> { it.index }.thenBy { it.id })
            .toList()
    }

    private fun JsonNode.toInterfaceOrNull(detailsMode: Boolean): BaseInterface? {
        val id = path("id").textOrEmpty()
        val index = path("index").takeIf { it.isInt }?.asInt()
        val interfaceName = path("interface-name").textOrEmpty()
        val typeAlias = path("type").textOrEmpty()
        val description = path("description").textOrEmpty()

        if (
            id.isBlank() ||
            index == null ||
            interfaceName.isBlank() ||
            typeAlias.isBlank() ||
            typeAlias == PORT_TYPE
        ) {
            return null
        }

        val common = InterfaceCommon(
            id = id,
            index = index,
            name = interfaceName,
            type = typeAlias,
            description = description,
        )
        if (!detailsMode) {
            return common.toBaseInterface()
        }

        return toTypedInterface(common)
    }

    private fun CliInterfaceBlock.toInterfaceOrNull(detailsMode: Boolean): BaseInterface? {
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

        val common = InterfaceCommon(
            id = id,
            index = index,
            name = interfaceName,
            type = typeAlias,
            description = description,
        )
        if (!detailsMode) {
            return common.toBaseInterface()
        }

        return toTypedInterface(common)
    }

    private fun JsonNode.toTypedInterface(common: InterfaceCommon): BaseInterface {
        if (common.type == WIREGUARD_TYPE) {
            val wireguardNode = path("wireguard")

            return WireguardInterface(
                id = common.id,
                index = common.index,
                name = common.name,
                type = common.type,
                description = common.description,
                link = textOrNull("link"),
                connected = textOrNull("connected"),
                state = textOrNull("state"),
                address = textOrNull("address"),
                mask = textOrNull("mask"),
                mtu = intOrNull("mtu"),
                uptime = longOrNull("uptime"),
                isGlobal = booleanOrNull("global") ?: false,
                securityLevel = textOrNull("security-level"),
                publicKey = wireguardNode.textOrNull("public-key"),
                listenPort = wireguardNode.intOrNull("listen-port"),
                status = wireguardNode.textOrNull("status"),
                asc = wireguardNode.asc().ifEmpty { asc() },
                peers = wireguardNode.path("peer")
                    .takeIf { it.isArray }
                    ?.mapNotNull { peer -> peer.toWireguardPeerOrNull() }
                    .orEmpty(),
            )
        }

        if (isInternetProvider()) {
            return InternetProviderInterface(
                id = common.id,
                index = common.index,
                name = common.name,
                type = common.type,
                description = common.description,
                link = textOrNull("link"),
                connected = textOrNull("connected"),
                state = textOrNull("state"),
                address = textOrNull("address"),
                mask = textOrNull("mask"),
                mtu = intOrNull("mtu"),
                uptime = longOrNull("uptime"),
                isGlobal = booleanOrNull("global") ?: false,
                isDefaultGateway = booleanOrNull("defaultgw"),
                priority = intOrNull("priority"),
                securityLevel = textOrNull("security-level"),
            )
        }

        return common.toBaseInterface()
    }

    private fun CliInterfaceBlock.toTypedInterface(common: InterfaceCommon): BaseInterface {
        if (common.type == WIREGUARD_TYPE) {
            return WireguardInterface(
                id = common.id,
                index = common.index,
                name = common.name,
                type = common.type,
                description = common.description,
                link = field("link"),
                connected = field("connected"),
                state = field("state"),
                address = field("address"),
                mask = field("mask"),
                mtu = field("mtu")?.toIntOrNull(),
                uptime = field("uptime")?.toLongOrNull(),
                isGlobal = field("global").toBooleanOrFalse(),
                securityLevel = field("security-level"),
                publicKey = nestedField("wireguard", "public-key"),
                listenPort = nestedField("wireguard", "listen-port")?.toIntOrNull(),
                status = nestedField("wireguard", "status"),
                asc = wireguardAsc(),
                peers = wireguardPeers(),
            )
        }

        if (isInternetProvider()) {
            return InternetProviderInterface(
                id = common.id,
                index = common.index,
                name = common.name,
                type = common.type,
                description = common.description,
                link = field("link"),
                connected = field("connected"),
                state = field("state"),
                address = field("address"),
                mask = field("mask"),
                mtu = field("mtu")?.toIntOrNull(),
                uptime = field("uptime")?.toLongOrNull(),
                isGlobal = field("global").toBooleanOrFalse(),
                isDefaultGateway = field("defaultgw")?.toBooleanOrNull(),
                priority = field("priority")?.toIntOrNull(),
                securityLevel = field("security-level"),
            )
        }

        return common.toBaseInterface()
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

        fun nestedField(section: String, field: String): String? {
            val sectionHeader = "$section:"
            val fieldRegex = Regex("""^\s*${Regex.escape(field)}:\s*(.*)$""")
            var inSection = false

            body.lineSequence().forEach { line ->
                val trimmed = line.trim()
                if (!inSection) {
                    inSection = trimmed == sectionHeader
                    return@forEach
                }

                if (line.isTopLevelField() && trimmed != sectionHeader) {
                    return null
                }

                fieldRegex.matchEntire(line)
                    ?.groupValues
                    ?.get(1)
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { return it }
            }

            return null
        }

        fun wireguardPeers(): List<WireguardPeer> {
            val peerBlocks = mutableListOf<MutableList<String>>()
            var inWireguard = false
            var currentPeer: MutableList<String>? = null

            body.lineSequence().forEach { line ->
                val trimmed = line.trim()
                if (!inWireguard) {
                    inWireguard = trimmed == "wireguard:"
                    return@forEach
                }

                if (line.isTopLevelField()) {
                    return@forEach
                }

                if (trimmed.startsWith("peer,")) {
                    currentPeer = mutableListOf(line).also { peerBlocks += it }
                    return@forEach
                }

                currentPeer?.add(line)
            }

            return peerBlocks.mapNotNull { lines -> lines.toWireguardPeerOrNull() }
        }

        fun wireguardAsc(): Map<String, String> {
            val ascLine = nestedField("wireguard", "asc")
            if (ascLine != null) {
                return ascLine.toAscMap(ASC_PARAMETER_ORDER)
            }

            val flatAscLine = wireguardAscLine()
            if (flatAscLine != null) {
                return flatAscLine.toAscMap(ASC_PARAMETER_ORDER)
            }

            val result = linkedMapOf<String, String>()
            ASC_PARAMETER_ORDER.forEach { parameter ->
                (nestedField("wireguard", parameter) ?: field(parameter))?.let { value ->
                    result[parameter] = value
                }
            }
            return result
        }

        fun wireguardAscLine(): String? {
            val regex = Regex("""^\s*wireguard\s+asc(?::|\s)\s*(.*)$""")

            return body.lineSequence()
                .mapNotNull { line ->
                    regex.matchEntire(line)
                        ?.groupValues
                        ?.get(1)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                }
                .firstOrNull()
        }

        fun isInternetProvider(): Boolean {
            return (field("global")?.toBooleanOrNull() ?: false) ||
                    field("defaultgw") != null ||
                    field("priority") != null
        }

        private fun List<String>.toWireguardPeerOrNull(): WireguardPeer? {
            val header = firstOrNull()?.trim().orEmpty()
            val publicKey = Regex("""peer,\s+public-key\s*=\s*"([^"]+)":?""")
                .matchEntire(header)
                ?.groupValues
                ?.get(1)
                ?: field("public-key")
                ?: return null

            return WireguardPeer(
                publicKey = publicKey,
                description = field("description").orEmpty(),
                localPort = field("local-port")?.toIntOrNull(),
                remotePort = field("remote-port")?.toIntOrNull(),
                via = field("via"),
                localEndpointAddress = field("local-endpoint-address"),
                remoteEndpointAddress = field("remote-endpoint-address"),
                rxBytes = field("rxbytes")?.toLongOrNull(),
                txBytes = field("txbytes")?.toLongOrNull(),
                lastHandshake = field("last-handshake")?.toLongOrNull(),
                isOnline = field("online")?.toBooleanOrNull(),
                isEnabled = field("enabled")?.toBooleanOrNull(),
                fwmark = field("fwmark")?.toLongOrNull(),
            )
        }

        private fun List<String>.field(name: String): String? {
            val regex = Regex("""^\s*${Regex.escape(name)}:\s*(.*)$""")

            return asSequence()
                .mapNotNull { line ->
                    regex.matchEntire(line)
                        ?.groupValues
                        ?.get(1)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                }
                .firstOrNull()
        }

        private fun String.toBooleanOrNull(): Boolean? {
            return when (lowercase()) {
                "true", "yes", "on", "1" -> true
                "false", "no", "off", "0" -> false
                else -> null
            }
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

        private fun String.isTopLevelField(): Boolean {
            return isNotBlank() && first().isWhitespace().not() && contains(':')
        }
    }

    private fun JsonNode.toWireguardPeerOrNull(): WireguardPeer? {
        val publicKey = textOrNull("public-key") ?: return null

        return WireguardPeer(
            publicKey = publicKey,
            description = textOrNull("description").orEmpty(),
            localPort = intOrNull("local-port"),
            remotePort = intOrNull("remote-port"),
            via = textOrNull("via"),
            localEndpointAddress = textOrNull("local-endpoint-address"),
            remoteEndpointAddress = textOrNull("remote-endpoint-address"),
            rxBytes = longOrNull("rxbytes"),
            txBytes = longOrNull("txbytes"),
            lastHandshake = longOrNull("last-handshake"),
            isOnline = booleanOrNull("online"),
            isEnabled = booleanOrNull("enabled"),
            fwmark = longOrNull("fwmark"),
        )
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
            textOrNull(parameter)?.let { value ->
                result[parameter] = value
            }
        }
        return result
    }

    private fun JsonNode.isInternetProvider(): Boolean {
        return booleanOrNull("global") == true ||
                hasNonNull("defaultgw") ||
                hasNonNull("priority")
    }

    private fun JsonNode.textOrNull(field: String): String? {
        val value = path(field)
        if (value.isMissingNode || value.isNull) return null

        return value.asText().takeIf { it.isNotBlank() }
    }

    private fun JsonNode.textOrEmpty(): String {
        return if (isMissingNode || isNull) "" else asText()
    }

    private fun JsonNode.intOrNull(field: String): Int? {
        val value = path(field)
        if (value.isMissingNode || value.isNull) return null
        if (value.isInt) return value.asInt()

        return value.asText().toIntOrNull()
    }

    private fun JsonNode.longOrNull(field: String): Long? {
        val value = path(field)
        if (value.isMissingNode || value.isNull) return null
        if (value.isLong || value.isInt) return value.asLong()

        return value.asText().toLongOrNull()
    }

    private fun JsonNode.booleanOrNull(field: String): Boolean? {
        val value = path(field)
        if (value.isMissingNode || value.isNull) return null
        if (value.isBoolean) return value.asBoolean()

        return value.asText().toBooleanOrNull()
    }

    private fun String?.toBooleanOrFalse(): Boolean {
        return this?.toBooleanOrNull() ?: false
    }

    private fun String.toBooleanOrNull(): Boolean? {
        return when (lowercase()) {
            "true", "yes", "on", "1" -> true
            "false", "no", "off", "0" -> false
            else -> null
        }
    }

    private data class InterfaceCommon(
        val id: String,
        val index: Int,
        val name: String,
        val type: String,
        val description: String,
    )

    private fun InterfaceCommon.toBaseInterface(): BaseInterface {
        return BaseInterface(
            id = id,
            index = index,
            name = name,
            type = type,
            description = description,
        )
    }

    private companion object {
        private const val PORT_TYPE = "Port"
        private const val WIREGUARD_TYPE = "Wireguard"

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

        private val LINE_SEPARATOR = Regex("""(?m)(?=^Interface,\s+name\s*=\s*")""")
        private val INTERFACE_HEADER = Regex("""Interface,\s+name\s*=\s*"([^"]*)":?""")
    }
}

internal fun String.toAscMap(parameterOrder: List<String>): Map<String, String> {
    return tokenizeAscValues().toAscMap(parameterOrder)
}

private fun String.tokenizeAscValues(): List<String> {
    return Regex(""""(?:[^"\\]|\\.)*"|\S+""")
        .findAll(trim())
        .map { match ->
            match.value
                .removeSurrounding("\"")
                .replace("\\\"", "\"")
        }
        .toList()
}

internal fun List<String>.toAscMap(parameterOrder: List<String>): Map<String, String> {
    return parameterOrder
        .zip(this)
        .toMap()
}
