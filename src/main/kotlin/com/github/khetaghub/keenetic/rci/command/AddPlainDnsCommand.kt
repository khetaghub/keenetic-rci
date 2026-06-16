package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.PlainDns
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliString
import com.github.khetaghub.keenetic.rci.utils.toCliToken
import com.github.khetaghub.keenetic.rci.utils.toJsonField

/** Adds a plain DNS resolver. */
class AddPlainDnsCommand(
    plainDns: PlainDns,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "ip": {
              "name-server": {
                "address": "${escapeJson(plainDns.address)}"${plainDns.domain.toJsonField("domain")}${plainDns.interfaceName.toJsonField("interface")}
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single(
        buildString {
            append("ip name-server ")
            append(plainDns.address.toCliToken())

            if (plainDns.domain != null || plainDns.interfaceName != null) {
                append(" ")
                append(plainDns.domain.orEmpty().toCliString())
            }

            plainDns.interfaceName?.let { interfaceName ->
                append(" on ")
                append(interfaceName.toCliToken())
            }
        }
    )
}
