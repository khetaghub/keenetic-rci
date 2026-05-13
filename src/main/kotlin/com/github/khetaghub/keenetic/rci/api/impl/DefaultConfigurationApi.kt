package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.ConfigurationApi
import com.github.khetaghub.keenetic.rci.api.LastChange
import com.github.khetaghub.keenetic.rci.command.*
import com.github.khetaghub.keenetic.rci.validator.ConfigurationApiValidator

internal class DefaultConfigurationApi(
    private val executor: RciCommandExecutor,
) : ConfigurationApi {

    override fun lastChange(): LastChange {
        return executor.execute(GetLastChangeCommand())
    }

    override fun save() {
        executor.executeWithoutResponse(SystemConfigurationSaveCommand())
    }

    override fun enableFailSafeTimer(seconds: Int) {
        ConfigurationApiValidator.validateFailSafeModeSeconds(seconds)
        executor.executeWithoutResponse(SystemConfigurationFailSafeTimerCommand(seconds))
    }

    override fun disableFailSafeTimer() {
        executor.executeWithoutResponse(SystemConfigurationDisableFailSafeTimerCommand())
    }

    override fun failSafeKeepAlive() {
        executor.executeWithoutResponse(SystemConfigurationFailSafeKeepAliveCommand())
    }

    override fun failSafeCommit() {
        executor.executeWithoutResponse(SystemConfigurationFailSafeCommitCommand())
    }

    override fun failSafeRollback() {
        executor.executeWithoutResponse(SystemConfigurationFailSafeRollbackCommand())
    }

}
