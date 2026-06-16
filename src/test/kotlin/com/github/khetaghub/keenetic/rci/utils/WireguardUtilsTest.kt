package com.github.khetaghub.keenetic.rci.utils

import com.github.khetaghub.keenetic.rci.api.WireguardConfig
import com.github.khetaghub.keenetic.rci.api.WireguardInterfaceConfig
import com.github.khetaghub.keenetic.rci.api.WireguardPeerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File

class WireguardUtilsTest {

    @Test
    @DisplayName("Parse WireGuard config")
    fun parseWgConfig() {
        val configAsString = javaClass.classLoader.getResource("wg/wg.conf")!!.readText()

        val parsedConfig = WireguardUtils.parseConfig(configAsString)
        val expectedConfig = WireguardConfig(
            interfaceConfig = WireguardInterfaceConfig(
                addresses = listOf("10.99.0.42/32"),
                dnsServers = listOf("9.9.9.9", "149.112.112.112"),
                privateKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
            ),
            peers = listOf(
                WireguardPeerConfig(
                    publicKey = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=",
                    presharedKey = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=",
                    allowedIps = listOf("0.0.0.0/0", "::/0"),
                    endpoint = "203.0.113.10:51820",
                    persistentKeepalive = 25,
                ),
            ),
        )
        assertThat(parsedConfig).isEqualTo(expectedConfig)

        val stringifiedConfig = WireguardUtils.configToString(parsedConfig)
        assertThat(stringifiedConfig.trimEnd().replace("\r\n", "\n"))
            .isEqualTo(configAsString.trimEnd().replace("\r\n", "\n"))
    }

    @Test
    @DisplayName("Parse AmneziaWG 1.0 config")
    fun parseAwg15Config() {
        val configAsString = javaClass.classLoader.getResource("wg/awg-1_5.conf")!!.readText()

        val parsedConfig = WireguardUtils.parseConfig(configAsString)
        val expectedConfig = WireguardConfig(
            interfaceConfig = WireguardInterfaceConfig(
                addresses = listOf("10.99.0.47/32"),
                dnsServers = listOf("9.9.9.9", "149.112.112.112"),
                privateKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                asc = mapOf(
                    "Jc" to "4",
                    "Jmin" to "10",
                    "Jmax" to "50",
                    "S1" to "136",
                    "S2" to "96",
                    "H1" to "1111111111",
                    "H2" to "2222222222",
                    "H3" to "3333333333",
                    "H4" to "444444444",
                ),
            ),
            peers = listOf(
                WireguardPeerConfig(
                    publicKey = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=",
                    presharedKey = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=",
                    allowedIps = listOf("0.0.0.0/0", "::/0"),
                    endpoint = "203.0.113.47:51820",
                    persistentKeepalive = 25,
                ),
            ),
        )
        assertThat(parsedConfig).isEqualTo(expectedConfig)

        val stringifiedConfig = WireguardUtils.configToString(parsedConfig)
        assertThat(stringifiedConfig.trimEnd().replace("\r\n", "\n"))
            .isEqualTo(configAsString.trimEnd().replace("\r\n", "\n"))
    }

    @Test
    @DisplayName("Parse AmneziaWG 2.0 config")
    fun parseAwg20Config() {
        val configAsString = javaClass.classLoader.getResource("wg/awg-2_0.conf")!!.readText()

        val parsedConfig = WireguardUtils.parseConfig(configAsString)
        val expectedConfig = WireguardConfig(
            interfaceConfig = WireguardInterfaceConfig(
                addresses = listOf("10.99.0.22/32"),
                dnsServers = listOf("9.9.9.9", "149.112.112.112"),
                privateKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
                asc = mapOf(
                    "Jc" to "5",
                    "Jmin" to "10",
                    "Jmax" to "50",
                    "S1" to "137",
                    "S2" to "60",
                    "S3" to "3",
                    "S4" to "13",
                    "H1" to "111111111-222222222",
                    "H2" to "333333333-444444444",
                    "H3" to "555555555-666666666",
                    "H4" to "777777777-888888888",
                    "I1" to "<r 2><b 0x85800001000100000000076578616d706c6503636f6d0000010001c00c000100010000012c00045db8d822>",
                    "I2" to "",
                    "I3" to "",
                    "I4" to "",
                    "I5" to "",
                ),
            ),
            peers = listOf(
                WireguardPeerConfig(
                    publicKey = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=",
                    presharedKey = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC=",
                    allowedIps = listOf("0.0.0.0/0", "::/0"),
                    endpoint = "203.0.113.22:51820",
                    persistentKeepalive = 25,
                ),
            ),
        )
        assertThat(parsedConfig).isEqualTo(expectedConfig)

        val stringifiedConfig = WireguardUtils.configToString(parsedConfig)
        assertThat(stringifiedConfig.trimEnd().replace("\r\n", "\n"))
            .isEqualTo(configAsString.trimEnd().replace("\r\n", "\n"))
    }

    @Test
    @DisplayName("Parse config files")
    fun configFile() {
        val configFiles = listOf(
            "src/test/resources/wg/wg.conf",
            "src/test/resources/wg/awg-1_5.conf",
            "src/test/resources/wg/awg-2_0.conf",
        )
        for (file in configFiles) {
            val configFile = WireguardUtils.parseFile(File(file))
            assertDoesNotThrow { WireguardUtils.parseConfig(configFile.fileContent) }
        }
    }

}
