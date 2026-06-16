package com.github.khetaghub.keenetic.rci.api

/** DNS operations. */
interface DnsApi {

    /** Returns all configured DNS resolvers, including plain DNS, DNS-over-TLS, and DNS-over-HTTPS. */
    fun getList(): List<Dns>

    /** Returns configured plain DNS resolvers that use standard unencrypted DNS queries. */
    fun getPlainList(): List<PlainDns>

    /** Returns configured secured DNS resolvers, including DNS-over-TLS and DNS-over-HTTPS. */
    fun getSecuredList(): List<SecuredDns>

    /** Adds a plain DNS resolver that uses standard unencrypted DNS queries. */
    fun add(plainDns: PlainDns)

    /** Adds a DNS-over-TLS resolver. */
    fun add(dnsOverTls: DnsOverTls)

    /** Adds a DNS-over-HTTPS resolver. */
    fun add(dnsOverHttps: DnsOverHttps)

    /** Deletes a plain DNS resolver by address. */
    fun deletePlainByAddress(address: String)

    /** Deletes all configured plain DNS resolvers. */
    fun deleteAllPlain()

    /** Deletes a DNS-over-TLS resolver by address. */
    fun deleteDnsOverTlsByAddress(address: String)

    /** Deletes all configured DNS-over-TLS resolvers. */
    fun deleteAllDnsOverTls()

    /** Deletes a DNS-over-HTTPS resolver by endpoint URL. */
    fun deleteDnsOverHttpsByAddress(address: String)

    /** Deletes all configured DNS-over-HTTPS resolvers. */
    fun deleteAllDnsOverHttps()

    /** Deletes all configured secured DNS resolvers, including DNS-over-TLS and DNS-over-HTTPS. */
    fun deleteAllSecured()

    /** Deletes all configured DNS resolvers, including plain DNS, DNS-over-TLS, and DNS-over-HTTPS. */
    fun deleteAll()

    /** Returns whether DNS servers automatically received from the provider are used on the selected interface. */
    fun getProviderDns(interfaceName: String): ProviderDnsState

    /** Enables or ignores DNS servers automatically received from the provider on the selected interface. */
    fun setProviderDns(interfaceName: String, ipVersion: ProviderDnsIpVersion, enabled: Boolean)

}

enum class ProviderDnsIpVersion {
    IPV4,
    IPV6,
    ALL,
}

data class ProviderDnsState(
    val ipv4: Boolean,
    val ipv6: Boolean,
)

/** Base type for DNS resolver definitions. */
sealed interface Dns {
    val address: String
    val interfaceName: String?
}

/** Base type for encrypted DNS resolver definitions. */
sealed interface SecuredDns : Dns

/**
 * Plain DNS resolver that uses standard unencrypted DNS queries.
 *
 * @property address IP address or domain name of the DNS resolver.
 * @property domain Domain that should use this DNS resolver.
 * @property interfaceName Network interface used to send DNS queries.
 */
data class PlainDns(
    override val address: String,
    val domain: String? = null,
    override val interfaceName: String?,
) : Dns

/**
 * DNS resolver available over DNS-over-TLS.
 *
 * @property address IP address or domain name of the DNS resolver.
 * @property tlsDomainName Domain name used to validate the resolver TLS certificate.
 * @property spki Expected SPKI fingerprint for additional public key validation.
 * @property domain Domain that should use this DNS resolver.
 * @property interfaceName Network interface used to send DNS queries.
 */
data class DnsOverTls(
    override val address: String,
    val tlsDomainName: String,
    val spki: String? = null,
    val domain: String? = null,
    override val interfaceName: String? = null
) : SecuredDns

/**
 * DNS resolver available over DNS-over-HTTPS.
 *
 * @property address DNS-over-HTTPS endpoint URL.
 * @property spki Expected SPKI fingerprint for additional public key validation.
 * @property domain Domain that should use this DNS resolver.
 * @property interfaceName Network interface used to send DNS queries.
 */
data class DnsOverHttps(
    override val address: String,
    val spki: String? = null,
    val domain: String? = null,
    override val interfaceName: String? = null
) : SecuredDns
