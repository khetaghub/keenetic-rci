package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

/** Deletes a DNS-over-TLS resolver by address. */
class DeleteDnsOverTlsCommand(
    address: String,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "dns-proxy": {
              "tls": {
                "upstream": {
                  "address": "${escapeJson(address)}",
                  "no": true
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("no dns-proxy tls upstream ${address.toCliToken()}")
}
