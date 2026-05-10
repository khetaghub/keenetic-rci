package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.SystemApi
import com.github.khetaghub.keenetic.rci.api.Version
import com.github.khetaghub.keenetic.rci.command.GetVersionCommand
import com.github.khetaghub.keenetic.rci.command.SystemConfigurationSaveCommand

internal class DefaultSystemApi(
    private val executor: RciCommandExecutor,
) : SystemApi {

    override fun version(): Version {
        return executor.execute(GetVersionCommand())
    }

    override fun configurationSave() {
        executor.executeWithoutResponse(SystemConfigurationSaveCommand())
    }

}
