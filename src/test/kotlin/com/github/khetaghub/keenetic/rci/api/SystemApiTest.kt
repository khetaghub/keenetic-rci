package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class SystemApiTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun version_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        assertDoesNotThrow { api.system().version() }
    }

    @Test
    fun version_checkSameResponse() {
        TestConfiguration.checkSame { api -> api.system().version() }
    }

}
