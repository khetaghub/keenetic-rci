package com.github.khetaghub.keenetic.rci.api

/** System-level Keenetic operations. */
interface SystemApi {

    /** Reads firmware and platform information using `show version`. */
    fun version(): Version

    /**
     * Persists the current running configuration to startup configuration.
     *
     * Call this after mutating commands when changes must survive reboot.
     */
    fun configurationSave()

}

data class Version(
    val release: String,
    val sandbox: String,
    val title: String,
    val arch: String,
)

