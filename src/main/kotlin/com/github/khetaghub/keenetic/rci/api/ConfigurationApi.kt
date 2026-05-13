package com.github.khetaghub.keenetic.rci.api

import com.fasterxml.jackson.annotation.JsonProperty

/** Keenetic configuration operations. */
interface ConfigurationApi {

    /** Returns metadata about the last committed configuration change on the device. */
    fun lastChange(): LastChange

    /**
     * Persists the current running configuration to startup configuration.
     *
     * Call this after mutating commands when changes must survive reboot.
     */
    fun save()

    /**
     * Configures or reconfigures the fail-safe timer with the `reboot` action.
     *
     * The timer state is persistent across reboots and does not require an additional [save] call.
     * If the timer expires before [failSafeCommit] is called, the device rolls back pending changes and reboots.
     *
     * On current firmware the device may report `Bumped up to N seconds.` when fail-safe mode was already active.
     */
    fun enableFailSafeTimer(seconds: Int)

    /** Disables the fail-safe timer. */
    fun disableFailSafeTimer()

    /**
     * Silently restarts the active fail-safe timer.
     *
     * If fail-safe mode is inactive or there are no pending configuration changes, the device ignores the command.
     */
    fun failSafeKeepAlive()

    /**
     * Commits all pending configuration changes and stops the active fail-safe timer.
     */
    fun failSafeCommit()

    /**
     * Rolls back all pending configuration changes and reboots the device into rollback state.
     *
     * If there are no pending changes, the device ignores the command.
     * In practice, verify [lastChange] before relying on rollback automation: when `failSafe.unsaved == false`,
     * current firmware may return `Ignored a fail-safe rollback: no pending changes.`
     */
    fun failSafeRollback()

}

data class LastChange(
    val date: String,
    val agent: String,
    val user: String,
    val checksum: String,
    val easyconfig: Boolean,
    @JsonProperty("fail-safe")
    val failSafe: FailSafe
)

data class FailSafe(
    val action: String?,
    val unsaved: Boolean,
    @JsonProperty("time-left")
    val timeLeft: Int,
    val rollback: Boolean,
    val blocked: Boolean
)
