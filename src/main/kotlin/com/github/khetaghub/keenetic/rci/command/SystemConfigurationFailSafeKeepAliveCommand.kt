package com.github.khetaghub.keenetic.rci.command

/** Silently restarts an active fail-safe timer when pending configuration changes exist. */
class SystemConfigurationFailSafeKeepAliveCommand : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "system": {
              "configuration": {
                "fail-safe": {
                  "keep-alive": {}
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("system configuration fail-safe keep-alive")

}
