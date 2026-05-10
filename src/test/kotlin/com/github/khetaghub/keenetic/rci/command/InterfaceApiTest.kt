package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class InterfaceApiTest {

    @Test
    fun getInterfacesList() {
        TestConfiguration.checkSame { api -> api.interfaces()
            .getInterfacesList()
            .sortedBy { it.index }
        }
    }

}
