package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DomainGroup
import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliString
import com.github.khetaghub.keenetic.rci.utils.toCliToken

class AddDomainGroupCommand(
    domainGroup: DomainGroup,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody: String

    override val cliCommand = CliCommandView.Sequential(
        buildList {
            add("object-group fqdn ${domainGroup.name.toCliToken()}")

            if (domainGroup.description.isNotBlank()) {
                add("object-group fqdn ${domainGroup.name.toCliToken()} description ${domainGroup.description.toCliString()}")
            }

            domainGroup.addresses.distinct().forEach { address ->
                add("object-group fqdn ${domainGroup.name.toCliToken()} include ${address.toCliToken()}")
            }
        }
    )

    init {
        val description = domainGroup.description
            .takeIf { it.isNotBlank() }
            ?.let { """"description":"${escapeJson(it)}",""" }
            .orEmpty()

        val include = domainGroup.addresses.joinToString(",") { address ->
            """{"address":"${escapeJson(address)}"}"""
        }

        httpRequestBody = """
            [
              {
                "object-group": {
                  "fqdn": {
                    "${escapeJson(domainGroup.name)}": {
                      $description
                      "include": [
                        $include
                      ]
                    }
                  }
                }
              }
            ]
        """.trimIndent()
    }

}
