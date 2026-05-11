package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

/** Removes a DNS proxy route identified by domain group and interface. */
class DeleteDomainGroupRoutingRuleCommand(
    domainGroupName: String,
    interfaceName: String,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
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

    override val cliCommand = CliCommandView.Single("no dns-proxy route object-group ${domainGroupName.toCliToken()} ${interfaceName.toCliToken()}")

}
