package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.SecuredDns

class GetSecuredDnsCommand : HttpBatchCommand<List<SecuredDns>>, CliCommand<List<SecuredDns>> {

    override val httpRequestBody = """
        [
          {
            "show": {
              "sc": {
                "dns-proxy": {}
              }
            }
          }
        ]
    """.trimIndent()

    override val cliCommand = CliCommandView.Sequential(
        buildList {
            add("show running-config | grep \"tls upstream\"")
            add("show running-config | grep \"https upstream\"")
        }
    )

}
