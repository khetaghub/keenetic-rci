package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

/** Deletes a plain DNS resolver by address. */
class DeletePlainDnsCommand(
    address: String,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "ip": {
              "name-server": {
                "address": "${escapeJson(address)}",
                "no": true
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("no ip name-server ${address.toCliToken()}")
}
