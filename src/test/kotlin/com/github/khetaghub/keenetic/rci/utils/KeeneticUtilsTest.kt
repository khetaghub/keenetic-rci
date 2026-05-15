package com.github.khetaghub.keenetic.rci.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KeeneticUtilsTest {

    @Test
    fun test() {
        val escapedHex = "\\xd0\\x9c\\xd0\\xbe\\xd1\\x8f \\xd0\\xb3\\xd1\\x80\\xd1\\x83\\xd0\\xbf\\xd0\\xbf\\xd0\\xb0"
        val utf8String = KeeneticUtils.decodeEscapedHexToUtf8(escapedHex)
        assertThat(utf8String).isEqualTo("Моя группа")
    }
    
}
