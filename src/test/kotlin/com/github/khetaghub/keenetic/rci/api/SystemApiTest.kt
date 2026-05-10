package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.junit.jupiter.api.Test

class SystemApiTest {

    @Test
    fun version() {
        TestConfiguration.checkSame { api -> api.system().version() }
    }

}
