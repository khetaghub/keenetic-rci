package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

class DeleteDomainGroupRoutingRuleCommand(
    domainGroupName: String,
    interfaceName: String,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody: String
    override val cliCommand: CliCommandView

    init {
        if (domainGroupName.isBlank()) throw KeeneticRciException("Field 'domainGroupName' must not be blank")

        httpRequestBody = """
            [
              {
                "dns-proxy": {
                  "route": {
                    "group": "${escapeJson(domainGroupName)}",
                    "interface": "${escapeJson(interfaceName)}",  
                    "no": true  
                  }
                }
              }
            ]
        """.trimIndent()

        cliCommand = CliCommandView.Single("no dns-proxy route object-group ${domainGroupName.toCliToken()} ${interfaceName.toCliToken()}")
    }

}