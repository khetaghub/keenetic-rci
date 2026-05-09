package com.github.khetaghub.keenetic.rci.api

interface SystemApi {

    fun version(): Version

    fun configurationSave()

}

data class Version(
    val release: String,
    val sandbox: String,
    val title: String,
    val arch: String,
)

