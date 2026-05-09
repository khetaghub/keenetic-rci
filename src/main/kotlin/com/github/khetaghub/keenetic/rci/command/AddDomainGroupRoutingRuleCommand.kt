package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

class AddDomainGroupRoutingRuleCommand(
    dgRoutingRule: DomainGroupRoutingRule,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody: String
    override val cliCommand: CliCommandView

    init {
        if (dgRoutingRule.groupName.isBlank()) throw KeeneticRciException("Field 'groupName' must not be blank")
        if (dgRoutingRule.interfaceName.isBlank()) throw KeeneticRciException("Field 'interfaceName' must not be blank")

        httpRequestBody = """
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

        cliCommand = CliCommandView.Single(
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
}