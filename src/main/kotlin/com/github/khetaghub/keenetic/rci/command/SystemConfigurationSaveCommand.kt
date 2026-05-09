package com.github.khetaghub.keenetic.rci.command

class SystemConfigurationSaveCommand : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        {
          "system": {
            "configuration": {
              "save": {}
            }
          }
        }
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("system configuration save")

}
