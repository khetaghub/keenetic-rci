package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

class DeleteDomainGroupCommand(
    domainGroupName: String,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
            [
              {
                "object-group": {
                  "fqdn": {
                    "name": "${escapeJson(domainGroupName)}",
                    "no": true
                  }
                }
              }
            ]
        """.trimIndent()

    override val cliCommand = CliCommandView.Single("no object-group fqdn ${domainGroupName.toCliToken()}")

}
