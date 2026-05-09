package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DomainGroup

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
     * CLI-команда для получения текущей конфигурации устройства.
     *
     * Основная команда: `show running-config`
     *
     * На некоторых устройствах также поддерживается альтернативная недокументированная
     * команда: `show rc object-group fqdn`
     */
    override val cliCommand = CliCommandView.Single("show running-config")

}