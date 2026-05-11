package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

/** Adds a DNS proxy route for an FQDN object group through a selected interface. */
class AddDomainGroupRoutingRuleCommand(
    dgRoutingRule: DomainGroupRoutingRule,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
            [
              {
                "dns-proxy": {
                  "route": {
                    "group": "${escapeJson(dgRoutingRule.groupName)}",
                    "gateway": "",
                    "auto": ${dgRoutingRule.auto},
                    "reject": ${dgRoutingRule.reject},
                    "interface": "${escapeJson(dgRoutingRule.interfaceName)}"
                  }
                }
              }
            ]
        """.trimIndent()

    override val cliCommand = CliCommandView.Single(
        buildString {
            append("dns-proxy route object-group ")
            append(dgRoutingRule.groupName.toCliToken())
            append(" ")
            append(dgRoutingRule.interfaceName.toCliToken())

            if (dgRoutingRule.auto) {
                append(" auto")
            }

            if (dgRoutingRule.reject) {
                append(" reject")
            }
        }
    )

}
