package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DnsOverHttps
import com.github.khetaghub.keenetic.rci.utils.appendOptionalCliField
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken
import com.github.khetaghub.keenetic.rci.utils.toJsonField

/** Adds a DNS-over-HTTPS resolver. */
class AddDnsOverHttpsCommand(
    dnsOverHttps: DnsOverHttps,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "dns-proxy": {
              "https": {
                "upstream": {
                  "url": "${escapeJson(dnsOverHttps.address)}",
                  "format": "dnsm"${dnsOverHttps.spki.toJsonField("spki")}${dnsOverHttps.domain.toJsonField("domain")}${dnsOverHttps.interfaceName.toJsonField("interface")}
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single(
        buildString {
            append("dns-proxy https upstream ")
            append(dnsOverHttps.address.toCliToken())
            appendOptionalCliField("spki", dnsOverHttps.spki)
            appendOptionalCliField("domain", dnsOverHttps.domain)
            dnsOverHttps.interfaceName?.let { interfaceName ->
                append(" on ")
                append(interfaceName.toCliToken())
            }
        }
    )
}
