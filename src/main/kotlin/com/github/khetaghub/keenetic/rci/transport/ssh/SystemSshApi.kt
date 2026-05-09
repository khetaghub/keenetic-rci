package com.github.khetaghub.keenetic.rci.transport.ssh

import com.github.khetaghub.keenetic.rci.api.KeeneticTransport
import com.github.khetaghub.keenetic.rci.api.SystemApi
import com.github.khetaghub.keenetic.rci.api.Version
import com.github.khetaghub.keenetic.rci.command.GetVersionCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.command.SystemConfigurationSaveCommand
import com.github.khetaghub.keenetic.rci.parser.ResponseParser

class SystemSshApi(
    private val transport: KeeneticTransport,
    private val responseParser: ResponseParser,
) : SystemApi {

    override fun version(): Version {
        val command = GetVersionCommand()
        val response = transport.execute(command)
        val parsedResponse = responseParser.parse(RciCommandType.CLI, command, response)
        return parsedResponse
    }

    override fun configurationSave() {
        val command = SystemConfigurationSaveCommand()
        transport.execute(command)
    }

}
