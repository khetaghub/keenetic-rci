package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.junit.jupiter.api.Test

class InterfaceApiTest {

    @Test
    fun getInterfacesList() {
        TestConfiguration.checkSame { api -> api.interfaces()
            .getInterfacesList()
            .sortedBy { it.index }
        }
    }

}
