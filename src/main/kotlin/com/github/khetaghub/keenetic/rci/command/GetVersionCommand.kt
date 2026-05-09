package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.Version

class GetVersionCommand : HttpBatchCommand<Version>, CliCommand<Version> {

    override val httpRequestBody = """
        {
          "show": {
            "version": {}
          }
        }
    """.trimIndent()

    override val cliCommand = CliCommandView.Single("show version")

}
