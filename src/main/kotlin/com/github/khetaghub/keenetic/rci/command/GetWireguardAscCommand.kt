package com.github.khetaghub.keenetic.rci.command

/** Reads WireGuard ASC parameters from the device configuration. */
class GetWireguardAscCommand :
    HttpBatchCommand<Map<String, Map<String, String>>>, CliCommand<Map<String, Map<String, String>>> {

    override val httpRequestBody = """
        [
          {
            "show": {
              "sc": {
                "interface": {}
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("show running-config")

}
