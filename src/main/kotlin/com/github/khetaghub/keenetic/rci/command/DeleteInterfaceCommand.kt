package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.escapeJson
import com.github.khetaghub.keenetic.rci.utils.toCliToken

/** Delete the interface by name. */
class DeleteInterfaceCommand(interfaceName: String) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "interface": {
                "name": "${escapeJson(interfaceName)}",
                "no": true
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("no interface ${interfaceName.toCliToken()}")

}
