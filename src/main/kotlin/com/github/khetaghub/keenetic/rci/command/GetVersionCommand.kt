package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.Version

/** Reads firmware and platform information from NDMS. */
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
