package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.WireguardConfig
import com.github.khetaghub.keenetic.rci.utils.WireguardUtils
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliString
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
                  "filename": "${escapeJson(interfaceDescription ?: interfaceName )}"
                }
              }
            }
          }
        ]
    """.trimIndent()

    /**
     * CLI command and context structure:
     *
     * root
     * ├── interface {interfaceName}
     * │   ├── description "{interfaceDescription}"
     * │   ├── ip address {IPv4 address} {IPv4 mask}
     * │   ├── ipv6 address {IPv6 address}/{prefix}
     * │   ├── wireguard private-key {PrivateKey}
     * │   ├── wireguard listen-port {ListenPort}
     * │   ├── ip mtu {MTU}
     * │   ├── wireguard asc {Jc Jmin Jmax S1 S2 H1 H2 H3 H4 [S3 S4 I1 I2 I3 I4 I5]}
     * │   ├── wireguard peer {PublicKey}
     * │   │   ├── preshared-key {PresharedKey}
     * │   │   ├── allow-ips {AllowedIPs item}
     * │   │   ├── endpoint {Endpoint}
     * │   │   ├── keepalive-interval {PersistentKeepalive}
     * │   │   └── exit
     * │   └── exit
     * └── ip name-server {DNS item} "" on {interfaceName}
     *
     * Mapping between CLI commands and WireGuard configuration fields:
     *
     * 1. `interface {interfaceName}`
     *    The `.conf` file does not contain the interface name. It is passed as a separate constructor argument.
     *
     * 2. `description "{interfaceDescription}"`
     *    The `.conf` file does not contain an interface description. This command is added only when
     *    `interfaceDescription` is not blank.
     *
     * 3. `ip address {address} {mask}`
     *    Source: `[Interface] Address`. Each IPv4 CIDR value becomes a separate command. For example,
     *    `10.8.1.47/32` becomes `ip address 10.8.1.47 255.255.255.255`.
     *
     * 4. `ipv6 address {address}/{prefix}`
     *    Source: `[Interface] Address`. Each IPv6 CIDR value becomes a separate command.
     *
     * 5. `wireguard private-key {key}`
     *    Source: `[Interface] PrivateKey`.
     *
     * 6. `wireguard listen-port {port}`
     *    Source: optional `[Interface] ListenPort` field.
     *
     * 7. `ip mtu {mtu}`
     *    Source: optional `[Interface] MTU` field.
     *
     * 8. `wireguard asc {values}`
     *    Source: `[Interface] Jc, Jmin, Jmax, S1, S2, H1, H2, H3, H4` and the optional extended block
     *    `S3, S4, I1, I2, I3, I4, I5`. The CLI argument order is fixed:
     *    `Jc Jmin Jmax S1 S2 H1 H2 H3 H4 [S3 S4 I1 I2 I3 I4 I5]`.
     *    Empty `I1-I5` values are passed as `""` to preserve their positions.
     *
     * 9. `wireguard peer {publicKey}`
     *    Source: `[Peer] PublicKey`. This command opens a context for a specific peer.
     *
     * 10. `preshared-key {key}`
     *     Source: optional `[Peer] PresharedKey` field.
     *
     * 11. `allow-ips {network}`
     *     Source: `[Peer] AllowedIPs`. Each list value becomes a separate command.
     *
     * 12. `endpoint {host:port}`
     *     Source: optional `[Peer] Endpoint` field.
     *
     * 13. `keepalive-interval {seconds}`
     *     Source: optional `[Peer] PersistentKeepalive` field.
     *
     * 14. `exit`
     *     Service command that is not present in the `.conf` file. It closes the `wireguard peer` context.
     *
     * 15. `exit`
     *     Service command that is not present in the `.conf` file. It closes the `interface {interfaceName}` context.
     *
     * 16. `ip name-server {dns} "" on {interfaceName}`
     *     Source: `[Interface] DNS`. Each DNS server becomes a separate command in the root context.
     *
     * Commands 9-14 are repeated for each `[Peer]` section.
     */
    override val cliCommand = CliCommandView.Contextual(
        buildList {
            val interfaceConfig = config.interfaceConfig

            add("interface $interfaceName")
            interfaceDescription
                ?.takeIf(String::isNotBlank)
                ?.let { add("description ${it.toCliString()}") }

            interfaceConfig.addresses.forEach { address ->
                add(address.toAddressCommand())
            }
            add("wireguard private-key ${interfaceConfig.privateKey}")
            interfaceConfig.listenPort?.let { add("wireguard listen-port $it") }
            interfaceConfig.mtu?.let { add("ip mtu $it") }
            interfaceConfig.asc.toAscCommandOrNull()?.let(::add)

            config.peers.forEach { peer ->
                add("wireguard peer ${peer.publicKey}")
                peer.presharedKey?.let { add("preshared-key $it") }
                peer.allowedIps.forEach { add("allow-ips $it") }
                peer.endpoint?.let { add("endpoint $it") }
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
            values += extendedValues.map { value ->
                value?.takeIf(String::isNotEmpty) ?: "\"\""
            }
        }

        return "wireguard asc ${values.joinToString(" ")}"
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
