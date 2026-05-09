package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

class DeleteDomainGroupCommand(
    domainGroupName: String,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody: String
    override val cliCommand: CliCommandView

    init {
        if (domainGroupName.isBlank()) throw KeeneticRciException("Field 'domainGroupName' must not be blank")

        httpRequestBody = """
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

        cliCommand = CliCommandView.Single("no object-group fqdn ${domainGroupName.toCliToken()}")
    }

}