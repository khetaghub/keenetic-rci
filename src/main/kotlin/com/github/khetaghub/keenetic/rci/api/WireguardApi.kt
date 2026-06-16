package com.github.khetaghub.keenetic.rci.api

/** WireGuard VPN operations. */
interface WireguardApi {

    /**
     * Imports a WireGuard configuration from a regular configuration file.
     *
     * @param configFile WireGuard configuration file content and name.
     * @param interfaceName name of the WireGuard interface to create, for example `Wireguard0` or `Wireguard1`.
     */
    fun import(configFile: WireguardConfigFile, interfaceName: String)

    /**
     * Imports a WireGuard configuration from its structured description.
     *
     * @param config WireGuard configuration structure.
     * @param interfaceName name of the WireGuard interface to create, for example `Wireguard0` or `Wireguard1`.
     * @param interfaceDescription display name of the connection. If not specified, [interfaceName] is used.
     */
    fun import(config: WireguardConfig, interfaceName: String, interfaceDescription: String? = null)

    /**
     * Returns ASC parameters configured for WireGuard interfaces.
     *
     * The outer map key is the interface name, for example `Wireguard0`.
     * The inner map contains ASC parameter names and values as reported by NDMS,
     * for example `Jc`, `Jmin`, `Jmax`, `S1`, `S2`, and `H1`-`H4`.
     *
     * @return map where each key is a WireGuard interface name and each value is
     * a map of ASC parameter names to their configured values.
     */
    fun getAscParams(): Map<String, Map<String, String>>

    /**
     * Generates the next available WireGuard interface name.
     *
     * The name is based on existing interfaces matching the `WireguardN` pattern,
     * where `N` is a numeric suffix. If no matching interfaces exist, returns `Wireguard0`.
     *
     * @return next WireGuard interface name, for example `Wireguard0` or `Wireguard1`.
     */
    fun generateNextInterfaceName(): String

    /**
     * Enables or disables a WireGuard interface.
     *
     * @param interfaceName name of the WireGuard interface to update, for example `Wireguard0` or `Wireguard1`.
     * @param enabled `true` to bring the interface up, `false` to bring it down.
     */
    fun setEnabled(interfaceName: String, enabled: Boolean)

}

data class WireguardConfigFile(
    val fileContent: String,
    val fileName: String,
)

data class WireguardConfig(
    val interfaceConfig: WireguardInterfaceConfig,
    val peers: List<WireguardPeerConfig>,
)

data class WireguardInterfaceConfig(
    val addresses: List<String>,
    val dnsServers: List<String> = emptyList(),
    val privateKey: String,
    val listenPort: Int? = null,
    val mtu: Int? = null,
    val asc: Map<String, String> = emptyMap(),
)

data class WireguardPeerConfig(
    val publicKey: String,
    val presharedKey: String? = null,
    val allowedIps: List<String>,
    val endpoint: String? = null,
    val persistentKeepalive: Int? = null,
)
