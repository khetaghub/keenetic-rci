package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.LastChange

/** Reads metadata about the last configuration change recorded by the device. */
class GetLastChangeCommand : HttpBatchCommand<LastChange>, CliCommand<LastChange> {

    override val httpRequestBody = """
        [
          {
            "show": {
              "last-change": {}
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("show last-change")

}
