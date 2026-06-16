package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.api.PlainDns

class GetPlainDnsCommand : HttpCommand<List<PlainDns>>, CliCommand<List<PlainDns>> {

    override val httpRequestUrl = "/show/ip/name-server"

    override val cliCommand = CliCommandView.Single("show ip name-server")

}
