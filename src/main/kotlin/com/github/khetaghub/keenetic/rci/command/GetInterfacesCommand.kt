package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.BaseInterface

/** Reads the list of interfaces known to the device. */
class GetInterfacesCommand(
    val detailed: Boolean = false,
) : HttpBatchCommand<List<BaseInterface>>, CliCommand<List<BaseInterface>> {

    override val httpRequestBody = if (detailed) {
        """
            [
              {
                "show": {
                    "interface": {
                        "details": "yes"
                    }
                }
              }
            ]
        """.trimIndent()
    } else {
        """
            [
              {
                "show": {
                    "interface": {}
                }
              }
            ]
        """.trimIndent()
    }

    override val cliCommand = CliCommandView.Single("show interface")

}
