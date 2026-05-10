package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.Interface

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