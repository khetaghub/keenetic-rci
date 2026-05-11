package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule

/** Reads DNS proxy routes that bind FQDN object groups to interfaces. */
class GetDomainGroupRoutingRulesCommand :
    HttpBatchCommand<List<DomainGroupRoutingRule>>, CliCommand<List<DomainGroupRoutingRule>> {

    override val httpRequestBody = """
        [
          {
            "show": {
              "sc": {
                "dns-proxy": {
                  "route": {}
                }
              }
            }
          }
        ]
    """.trimIndent()

    /**
     * CLI command for reading the current device configuration.
     *
     * Primary command: `show running-config`
     *
     * Some devices also support an alternative undocumented command:
     * `show rc dns-proxy route`
     */
    override val cliCommand = CliCommandView.Single("show running-config")

}
