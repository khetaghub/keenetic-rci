package com.github.khetaghub.keenetic.rci.command

/** Commits all pending configuration changes and stops the active fail-safe timer. */
class SystemConfigurationFailSafeCommitCommand : HttpBatchCommand<Unit>, CliCommand<Unit> {

    override val httpRequestBody = """
        [
          {
            "system": {
              "configuration": {
                "fail-safe": {
                  "commit": {}
                }
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("system configuration fail-safe commit")

}
