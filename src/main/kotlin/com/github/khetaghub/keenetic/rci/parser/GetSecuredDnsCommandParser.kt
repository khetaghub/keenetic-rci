package com.github.khetaghub.keenetic.rci.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.khetaghub.keenetic.rci.api.DnsOverHttps
import com.github.khetaghub.keenetic.rci.api.DnsOverTls
import com.github.khetaghub.keenetic.rci.api.SecuredDns
import com.github.khetaghub.keenetic.rci.command.GetSecuredDnsCommand
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

class GetSecuredDnsCommandParser(
    private val objectMapper: ObjectMapper
) : Parser<List<SecuredDns>> {

    override val commandClass = GetSecuredDnsCommand::class

    override fun parseHttpResponse(
        commandType: RciCommandType,
        command: RciCommand<List<SecuredDns>>,
        response: String
    ): List<SecuredDns> {
        return try {
            val root = objectMapper.readTree(response)

            root.findDnsProxyNodes()
                .flatMap { dnsProxy ->
                    buildList {
                        addAll(parseDnsOverTls(dnsProxy))
                        addAll(parseDnsOverHttps(dnsProxy))
                    }
                }
        } catch (ex: Exception) {
            throw KeeneticRciException(
                "Failed to parse secured DNS response",
                ex
            )
        }
    }

    override fun parseCliOutput(
        commandType: RciCommandType,
        command: RciCommand<List<SecuredDns>>,
        response: String
    ): List<SecuredDns> {
        return response
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                when {
                    line.containsUpstream("tls") -> parseTlsUpstream(line)
                    line.containsUpstream("https") -> parseHttpsUpstream(line)
                    else -> null
                }
            }
            .toList()
    }

    private fun parseDnsOverTls(dnsProxy: JsonNode): List<DnsOverTls> {
        return dnsProxy
            .path("tls")
            .path("upstream")
            .mapNotNull { node ->
                val address = node.path("address").textOrNull()
                val sni = node.path("fqdn").textOrNull()
                    ?: node.path("sni").textOrNull()

                if (address == null || sni == null) {
                    return@mapNotNull null
                }

                DnsOverTls(
                    address = address,
                    tlsDomainName = sni,
                    spki = node.path("spki").textOrNull(),
                    domain = node.path("domain").textOrNull(),
                    interfaceName = node.path("interface").textOrNull()
                )
            }
    }

    private fun parseDnsOverHttps(dnsProxy: JsonNode): List<DnsOverHttps> {
        return dnsProxy
            .path("https")
            .path("upstream")
            .mapNotNull { node ->
                val uri = node.path("url").textOrNull()
                    ?: node.path("uri").textOrNull()
                    ?: return@mapNotNull null

                DnsOverHttps(
                    address = uri,
                    spki = node.path("spki").textOrNull(),
                    domain = node.path("domain").textOrNull(),
                    interfaceName = node.path("interface").textOrNull()
                )
            }
    }

    private fun JsonNode.findDnsProxyNodes(): List<JsonNode> {
        val result = mutableListOf<JsonNode>()

        fun visit(node: JsonNode) {
            when {
                node.isObject -> {
                    node.path("dns-proxy")
                        .takeIf(JsonNode::isObject)
                        ?.let(result::add)

                    node.fields().forEachRemaining { (_, child) -> visit(child) }
                }

                node.isArray -> node.forEach(::visit)
            }
        }

        visit(this)
        return result
    }

    private fun parseTlsUpstream(line: String): DnsOverTls? {
        val tokens = line.splitByWhitespaces()

        // The grep output can contain either the raw directive or a dns-proxy prefix.
        // tls upstream 1.1.1.1 sni cloudflare-dns.com
        // dns-proxy tls upstream 1.1.1.1 sni cloudflare-dns.com
        val upstreamIndex = tokens.upstreamIndex("tls")
            ?: return null
        if (upstreamIndex + 2 >= tokens.size) return null

        val address = tokens[upstreamIndex + 2]
        val sniIndex = tokens.indexOf("sni")

        val sni = if (sniIndex >= 0 && sniIndex + 1 < tokens.size) {
            tokens[sniIndex + 1]
        } else {
            return null
        }

        return DnsOverTls(
            address = address,
            tlsDomainName = sni,
            spki = null,
            domain = null,
            interfaceName = null
        )
    }

    private fun parseHttpsUpstream(line: String): DnsOverHttps? {
        val tokens = line.splitByWhitespaces()

        // The grep output can contain either the raw directive or a dns-proxy prefix.
        // https upstream https://1.1.1.1/dns-query dnsm
        // dns-proxy https upstream https://1.1.1.1/dns-query dnsm
        val upstreamIndex = tokens.upstreamIndex("https")
            ?: return null
        if (upstreamIndex + 2 >= tokens.size) return null

        return DnsOverHttps(
            address = tokens[upstreamIndex + 2],
            spki = null,
            domain = null,
            interfaceName = null
        )
    }

    private fun JsonNode.textOrNull(): String? {
        if (isMissingNode || isNull) return null

        val value = asText()
        return value.takeIf { it.isNotBlank() }
    }

    private fun String.splitByWhitespaces(): List<String> {
        return trim().split(Regex("\\s+"))
    }

    private fun String.containsUpstream(protocol: String): Boolean {
        return splitByWhitespaces().upstreamIndex(protocol) != null
    }

    private fun List<String>.upstreamIndex(protocol: String): Int? {
        return indices.firstOrNull { index ->
            this[index] == protocol &&
                    index + 1 < size &&
                    this[index + 1] == "upstream"
        }
    }
}
