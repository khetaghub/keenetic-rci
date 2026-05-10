package com.github.khetaghub.keenetic.rci.command

import com.github.khetaghub.keenetic.rci.TestConfiguration
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
class SystemApiTest {

    @Test
    fun version() {
        TestConfiguration.checkSame { api -> api.system().version() }
    }

}
