package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class InterfaceApiTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.github.khetaghub.keenetic.rci.TestConfiguration#apis")
    fun getList_checkTransport(@Suppress("UNUSED_PARAMETER") transport: String, api: KeeneticApi) {
        api.interfaces().getList()
    }

    @Test
    fun getList_checkSameResponse() {
        TestConfiguration.checkSame { api ->
            api.interfaces()
                .getList()
                .sortedBy { it.index }
        }
    }

}
