package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.DomainGroupRoutingRule

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
     * CLI-команда для получения текущей конфигурации устройства.
     *
     * Основная команда: `show running-config`
     *
     * На некоторых устройствах также поддерживается альтернативная недокументированная
     * команда: `show rc dns-proxy route`
     */
    override val cliCommand = CliCommandView.Single("show running-config")

}