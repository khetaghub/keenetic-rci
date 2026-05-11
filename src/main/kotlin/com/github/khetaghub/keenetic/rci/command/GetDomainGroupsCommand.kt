package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DomainGroup

/** Reads configured FQDN object groups. */
class GetDomainGroupsCommand : HttpBatchCommand<List<DomainGroup>>, CliCommand<List<DomainGroup>> {

    override val httpRequestBody = """
        [
          {
            "show": {
              "sc": {
                "object-group": {
                  "fqdn": {}
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
     * `show rc object-group fqdn`
     */
    override val cliCommand = CliCommandView.Single("show running-config")

}
