package com.github.khetaghub.keenetic.rci.validator

import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException

internal object ConfigurationApiValidator {

    fun validateFailSafeModeSeconds(seconds: Int) {
        if (seconds !in 60..86400) {
            throw KeeneticRciException(
                "Fail-safe timer interval must be between 60 and 86400 seconds, but was $seconds."
            )
        }
    }

}
