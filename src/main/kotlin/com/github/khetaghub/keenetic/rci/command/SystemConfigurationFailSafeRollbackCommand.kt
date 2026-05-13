package com.github.khetaghub.keenetic.rci.command

/** Rolls back all pending configuration changes and reboots the device into rollback state. */
class SystemConfigurationFailSafeRollbackCommand : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "system": {
              "configuration": {
                "fail-safe": {
                  "rollback": {}
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("system configuration fail-safe rollback")

}
