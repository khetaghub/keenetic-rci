package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DnsOverTls
import com.github.khetaghub.keenetic.rci.utils.appendOptionalCliField
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken
import com.github.khetaghub.keenetic.rci.utils.toJsonField

/** Adds a DNS-over-TLS resolver. */
class AddDnsOverTlsCommand(
    dnsOverTls: DnsOverTls,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "dns-proxy": {
              "tls": {
                "upstream": {
                  "address": "${escapeJson(dnsOverTls.address)}",
                  "fqdn": "${escapeJson(dnsOverTls.tlsDomainName)}"${dnsOverTls.spki.toJsonField("spki")}${dnsOverTls.domain.toJsonField("domain")}${dnsOverTls.interfaceName.toJsonField("interface")}
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single(
        buildString {
            append("dns-proxy tls upstream ")
            append(dnsOverTls.address.toCliToken())
            append(" sni ")
            append(dnsOverTls.tlsDomainName.toCliToken())
            appendOptionalCliField("spki", dnsOverTls.spki)
            appendOptionalCliField("domain", dnsOverTls.domain)
            dnsOverTls.interfaceName?.let { interfaceName ->
                append(" on ")
                append(interfaceName.toCliToken())
            }
        }
    )
}
