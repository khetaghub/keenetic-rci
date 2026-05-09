package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.junit.jupiter.api.Test

class SystemApiTest {

    @Test
    fun test() {
        TestConfiguration.checkSame { api -> api.system().version() }
    }

}