package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.WireguardConfig
import com.github.khetaghub.keenetic.rci.utils.WireguardUtils
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliString
import com.github.khetaghub.keenetic.rci.utils.toCliToken
import java.util.Base64

class WireguardImportConfigCommand(
    config: WireguardConfig,
    interfaceName: String,
    interfaceDescription: String?
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    private val encodedConfig: String = Base64.getEncoder()
        .encodeToString(WireguardUtils.configToString(config).toByteArray(Charsets.UTF_8))

    override val httpRequestBody = """
        [
          {
            "interface": {
              "wireguard": {
                "import": {
                  "import": "${escapeJson(encodedConfig)}",
                  "name": "",
                  "filename": "${escapeJson(interfaceDescription ?: interfaceName)}"
                }
              }
            }
          }
        ]
    """.trimIndent()

    // SSH cannot use the same base64 import endpoint as HTTP, so it recreates
    // the WireGuard interface through nested NDMS CLI contexts.
    override val cliCommand = CliCommandView.Contextual(
        buildList {
            val interfaceConfig = config.interfaceConfig

            val interfaceToken = interfaceName.toCliToken()

            add("interface $interfaceToken")
            interfaceDescription
                ?.takeIf(String::isNotBlank)
                ?.let { add("description ${it.toCliString()}") }

            interfaceConfig.addresses.forEach { address ->
                add(address.toAddressCommand())
            }
            add("wireguard private-key ${interfaceConfig.privateKey.toCliToken()}")
            interfaceConfig.listenPort?.let { add("wireguard listen-port $it") }
            interfaceConfig.mtu?.let { add("ip mtu $it") }
            interfaceConfig.asc.toAscCommandOrNull()?.let(::add)

            config.peers.forEach { peer ->
                add("wireguard peer ${peer.publicKey.toCliToken()}")
                peer.presharedKey?.let { add("preshared-key ${it.toCliToken()}") }
                peer.allowedIps.forEach { add("allow-ips ${it.toCliToken()}") }
                peer.endpoint?.let { add("endpoint ${it.toCliToken()}") }
                peer.persistentKeepalive?.let { add("keepalive-interval $it") }
                add("exit")
            }

            add("exit")

            //interfaceConfig.dnsServers.forEach { dnsServer ->
            //    add("ip name-server $dnsServer \"\" on $interfaceName")
            //}
        }
    )

    private fun String.toAddressCommand(): String {
        val address = substringBeforeLast('/')
        val prefixLength = substringAfterLast('/').toInt()

        return if (address.contains(':')) {
            "ipv6 address $address/$prefixLength"
        } else {
            "ip address $address ${prefixLength.toIpv4Mask()}"
        }
    }

    private fun Int.toIpv4Mask(): String {
        val mask = if (this == 0) 0L else (0xffffffffL shl (32 - this)) and 0xffffffffL
        return listOf(24, 16, 8, 0)
            .joinToString(".") { shift -> ((mask shr shift) and 0xff).toString() }
    }

    private fun Map<String, String>.toAscCommandOrNull(): String? {
        if (isEmpty()) {
            return null
        }

        val values = ASC_BASE_PARAMETER_ORDER.map { parameter ->
            entries
                .firstOrNull { it.key.equals(parameter, ignoreCase = true) }
                ?.value
                ?: return null
        }.toMutableList()

        val extendedValues = ASC_EXTENDED_PARAMETER_ORDER.map { parameter ->
            entries
                .firstOrNull { it.key.equals(parameter, ignoreCase = true) }
                ?.value
                ?: return@map null
        }
        if (extendedValues.any { it != null }) {
            values += extendedValues.map { it.orEmpty() }
        }

        return "wireguard asc ${values.joinToString(" ") { it.toAscCliValue() }}"
    }

    private fun String.toAscCliValue(): String {
        return when {
            isEmpty() -> "\"\""
            any(Char::isWhitespace) -> toCliString()
            else -> toCliToken()
        }
    }

    companion object {
        private val ASC_BASE_PARAMETER_ORDER = listOf(
            "Jc",
            "Jmin",
            "Jmax",
            "S1",
            "S2",
            "H1",
            "H2",
            "H3",
            "H4",
        )

        private val ASC_EXTENDED_PARAMETER_ORDER = listOf(
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
