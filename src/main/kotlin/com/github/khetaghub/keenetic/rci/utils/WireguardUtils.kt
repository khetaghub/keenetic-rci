package com.github.khetaghub.keenetic.rci.utils

import com.github.khetaghub.keenetic.rci.api.WireguardConfigFile
import com.github.khetaghub.keenetic.rci.api.WireguardConfig
import com.github.khetaghub.keenetic.rci.api.WireguardInterfaceConfig
import com.github.khetaghub.keenetic.rci.api.WireguardPeerConfig
import org.ini4j.Ini
import org.ini4j.Profile
import java.io.File
import java.io.StringReader
import java.io.StringWriter

object WireguardUtils {

    fun parseFile(configFile: File): WireguardConfigFile {
        val content = configFile.readText()
        return WireguardConfigFile(
            fileContent = content,
            fileName = configFile.nameWithoutExtension,
        )
    }

    fun parseConfig(config: String): WireguardConfig {
        val ini = newIni()
        try {
            ini.load(StringReader(config))
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid WireGuard INI config", e)
        }

        val unsupportedSections = ini.keys.filterNot {
            it.equals("interface", ignoreCase = true) || it.equals("peer", ignoreCase = true)
        }
        require(unsupportedSections.isEmpty()) {
            "Unsupported WireGuard sections: ${unsupportedSections.joinToString()}"
        }

        val interfaceSections = ini.sections("interface")
        require(interfaceSections.size == 1) {
            "WireGuard config must contain exactly one [Interface] section"
        }
        val interfaceSection = interfaceSections.single()

        return WireguardConfig(
            interfaceConfig = WireguardInterfaceConfig(
                addresses = interfaceSection.requiredList("address", "[Interface]"),
                dnsServers = interfaceSection.optionalList("dns"),
                privateKey = interfaceSection.required("privatekey", "[Interface]"),
                listenPort = interfaceSection.optionalInt("listenport", "[Interface]"),
                mtu = interfaceSection.optionalInt("mtu", "[Interface]"),
                asc = interfaceSection.toAscConfig(config),
            ),
            peers = ini.sections("peer").mapIndexed { index, section ->
                val sectionName = "[Peer #${index + 1}]"
                WireguardPeerConfig(
                    publicKey = section.required("publickey", sectionName),
                    presharedKey = section.value("presharedkey"),
                    allowedIps = section.requiredList("allowedips", sectionName),
                    endpoint = section.value("endpoint"),
                    persistentKeepalive = section.optionalInt("persistentkeepalive", sectionName),
                )
            },
        )
    }

    fun configToString(config: WireguardConfig): String {
        val ini = newIni()

        val interfaceConfig = config.interfaceConfig
        val interfaceSection = ini.add("Interface")
        interfaceSection.putList("Address", interfaceConfig.addresses)
        interfaceSection.putListIfNotEmpty("DNS", interfaceConfig.dnsServers)
        interfaceSection["PrivateKey"] = interfaceConfig.privateKey
        interfaceConfig.listenPort?.let { interfaceSection["ListenPort"] = it.toString() }
        interfaceConfig.mtu?.let { interfaceSection["MTU"] = it.toString() }
        interfaceConfig.asc.forEach { (name, value) ->
            require(name.isNotBlank()) { "ASC parameter name must not be blank" }
            require(STANDARD_INTERFACE_KEYS.none { it.equals(name, ignoreCase = true) }) {
                "ASC parameter '$name' conflicts with a WireGuard interface property"
            }
            interfaceSection[name] = value
        }

        config.peers.forEach { peer ->
            val peerSection = ini.add("Peer")
            peerSection["PublicKey"] = peer.publicKey
            peer.presharedKey?.let { peerSection["PresharedKey"] = it }
            peerSection.putList("AllowedIPs", peer.allowedIps)
            peer.endpoint?.let { peerSection["Endpoint"] = it }
            peer.persistentKeepalive?.let { peerSection["PersistentKeepalive"] = it.toString() }
        }

        return StringWriter()
            .also(ini::store)
            .toString()
            .replace(Regex("[ \t]+(?=\n|$)"), "")
    }

    private fun newIni(): Ini = Ini().apply {
        config.isMultiSection = true
        config.isEmptyOption = true
        config.isEscape = false
        config.lineSeparator = "\n"
    }

    private fun Profile.Section.toAscConfig(config: String): Map<String, String> {
        var inInterfaceSection = false
        val result = linkedMapOf<String, String>()

        config.lineSequence().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("[") && trimmedLine.endsWith("]")) {
                inInterfaceSection = trimmedLine
                    .removeSurrounding("[", "]")
                    .trim()
                    .equals("interface", ignoreCase = true)
                return@forEach
            }
            if (!inInterfaceSection || trimmedLine.isEmpty() ||
                trimmedLine.startsWith(";") || trimmedLine.startsWith("#")
            ) {
                return@forEach
            }

            val name = trimmedLine.substringBefore('=', missingDelimiterValue = "").trim()
            if (name.isNotEmpty() &&
                STANDARD_INTERFACE_KEYS.none { it.equals(name, ignoreCase = true) }
            ) {
                value(name)?.let { result[name] = it }
            }
        }

        return result
    }

    private fun Profile.Section.required(key: String, section: String): String =
        value(key) ?: throw IllegalArgumentException("Missing required property '$key' in $section")

    private fun Profile.Section.requiredList(key: String, section: String): List<String> =
        required(key, section).toValueList(key, section)

    private fun Profile.Section.optionalList(key: String): List<String> =
        value(key)?.toValueList(key, "[Interface]").orEmpty()

    private fun String.toValueList(key: String, section: String): List<String> =
        split(',')
            .map(String::trim)
            .also { values ->
                require(values.none(String::isEmpty)) {
                    "Property '$key' in $section contains an empty value"
                }
            }

    private fun Profile.Section.optionalInt(key: String, section: String): Int? =
        value(key)?.let { it.toIntOrNull() ?: invalidNumber(key, section) }

    private fun invalidNumber(key: String, section: String): Nothing =
        throw IllegalArgumentException("Property '$key' in $section must be a number")

    private fun Ini.sections(name: String): List<Profile.Section> =
        entries
            .filter { it.key.equals(name, ignoreCase = true) }
            .flatMap { getAll(it.key) }

    private fun Profile.Section.value(name: String): String? =
        entries
            .firstOrNull { it.key.equals(name, ignoreCase = true) }
            ?.value

    private fun Profile.Section.putList(name: String, values: List<String>) {
        require(values.isNotEmpty()) { "WireGuard property '$name' must not be empty" }
        this[name] = values.joinToString(", ")
    }

    private fun Profile.Section.putListIfNotEmpty(name: String, values: List<String>) {
        if (values.isNotEmpty()) {
            putList(name, values)
        }
    }

    private val STANDARD_INTERFACE_KEYS = setOf(
        "Address",
        "DNS",
        "PrivateKey",
        "ListenPort",
        "MTU",
    )
}
