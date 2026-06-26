package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.utils.WireguardUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WireguardImportConfigCommandTest {

    @Test
    fun cliCommand_quotesAscValuesWithSpaces() {
        val configAsString = javaClass.classLoader.getResource("wg/awg-2_0.conf")!!.readText()
        val config = WireguardUtils.parseConfig(configAsString)

        val command = WireguardImportConfigCommand(
            config = config,
            interfaceName = "Wireguard0",
            interfaceDescription = null,
        )

        val cliCommand = command.cliCommand as CliCommandView.Contextual

        assertThat(cliCommand.commands)
            .contains(
                "wireguard asc 5 10 50 137 60 111111111-222222222 333333333-444444444 " +
                        "555555555-666666666 777777777-888888888 3 13 " +
                        "\"<r 2><b 0x85800001000100000000076578616d706c6503636f6d0000010001c00c000100010000012c00045db8d822>\" " +
                        "\"\" \"\" \"\" \"\""
            )
    }
}
