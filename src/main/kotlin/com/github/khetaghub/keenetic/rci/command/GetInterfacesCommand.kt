package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.Interface

/** Reads the list of interfaces known to the device. */
class GetInterfacesCommand : HttpBatchCommand<List<Interface>>, CliCommand<List<Interface>> {

    override val httpRequestBody = """
        [
          {
            "show": {
                "interface": {}
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("show interface")

}
