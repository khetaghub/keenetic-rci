package com.github.khetaghub.keenetic.rci.command

/** Disables the persistent fail-safe timer. */
class SystemConfigurationDisableFailSafeTimerCommand : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "system": {
              "configuration": {
                "fail-safe": {
                  "timer": {
                    "no": true
                  }
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("no system configuration fail-safe timer")

}
