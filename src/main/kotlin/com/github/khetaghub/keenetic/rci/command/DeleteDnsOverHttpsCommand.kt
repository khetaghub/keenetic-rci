package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

/** Deletes a DNS-over-HTTPS resolver by endpoint URL. */
class DeleteDnsOverHttpsCommand(
    address: String,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "dns-proxy": {
              "https": {
                "upstream": {
                  "url": "${escapeJson(address)}",
                  "no": true
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("no dns-proxy https upstream ${address.toCliToken()}")
}
