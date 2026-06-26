package com.github.khetaghub.keenetic.rci.api

/** Interface-related Keenetic operations. */
interface InterfaceApi {

    /** Returns interfaces known to NDMS, ordered as reported by the device. */
    fun getList(): List<BaseInterface>

    /**
     * Returns internet provider interfaces with connection details.
     *
     * This call requests the detailed interface view from the device.
     */
    fun getProviderList(): List<InternetProviderInterface>

    /**
     * Returns WireGuard interfaces with connection details, ASC parameters, and peers.
     *
     * This call requests the detailed interface view from the device.
     */
    fun getWireguardList(): List<WireguardInterface>

    /** Deletes interface by name. */
    fun delete(interfaceName: String)

}

open class BaseInterface(
    open val id: String,
    open val index: Int,
    open val name: String,
    open val type: String,
    open val description: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BaseInterface

        return id == other.id &&
                index == other.index &&
                name == other.name &&
                type == other.type &&
                description == other.description
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + index
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    override fun toString(): String {
        return "BaseInterface(id=$id, index=$index, name=$name, type=$type, description=$description)"
    }
}

data class Ipv6Address(
    val address: String,
    val prefixLength: Int?,
    val proto: String?,
    val validLifetime: String?,
)

data class InternetProviderInterface(
    override val id: String,
    override val index: Int,
    override val name: String,
    override val type: String,
    override val description: String,
    val link: String?,
    val connected: String?,
    val state: String?,
    val address: String?,
    val mask: String?,
    val mtu: Int?,
    val uptime: Long?,
    val isGlobal: Boolean,
    val isDefaultGateway: Boolean?,
    val priority: Int?,
    val securityLevel: String?,
) : BaseInterface(id, index, name, type, description)

data class WireguardInterface(
    override val id: String,
    override val index: Int,
    override val name: String,
    override val type: String,
    override val description: String,
    val link: String?,
    val connected: String?,
    val state: String?,
    val address: String?,
    val mask: String?,
    val mtu: Int?,
    val uptime: Long?,
    val isGlobal: Boolean,
    val securityLevel: String?,
    val publicKey: String?,
    val listenPort: Int?,
    val status: String?,
    val asc: Map<String, String> = emptyMap(),
    val peers: List<WireguardPeer>,
) : BaseInterface(id, index, name, type, description)

data class WireguardPeer(
    val publicKey: String,
    val description: String,
    val localPort: Int?,
    val remotePort: Int?,
    val via: String?,
    val localEndpointAddress: String?,
    val remoteEndpointAddress: String?,
    val rxBytes: Long?,
    val txBytes: Long?,
    val lastHandshake: Long?,
    val isOnline: Boolean?,
    val isEnabled: Boolean?,
    val fwmark: Long?,
)
