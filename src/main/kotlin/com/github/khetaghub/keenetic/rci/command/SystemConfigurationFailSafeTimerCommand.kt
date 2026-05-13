package com.github.khetaghub.keenetic.rci.command

/** Configures or reconfigures a persistent fail-safe timer with the `reboot` action. */
class SystemConfigurationFailSafeTimerCommand(seconds: Int) : HttpBatchCommand<Unit>, CliCommand<Unit> {

    companion object {
        const val ACTIVE_PARAM_VALUE = "reboot"
    }

    override val httpRequestBody = """
        [
          {
            "system": {
              "configuration": {
                "fail-safe": {
                  "timer": {
                    "action": "$ACTIVE_PARAM_VALUE",
                    "interval": $seconds
                  }
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("system configuration fail-safe timer $ACTIVE_PARAM_VALUE $seconds")

}
