package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

class SetWireguardEnabledCommand(
    interfaceName: String,
    enabled: Boolean,
) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "interface": {
              "${escapeJson(interfaceName)}": {
                "up": $enabled
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single(
        "interface ${interfaceName.toCliToken()} ${if (enabled) "up" else "down"}"
    )

}
