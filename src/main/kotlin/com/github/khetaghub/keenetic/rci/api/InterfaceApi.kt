package com.github.khetaghub.keenetic.rci.api

interface InterfaceApi {

    /** Returns interfaces known to NDMS, ordered as reported by the device. */
    fun getInterfacesList(): List<Interface>

}

data class Interface(
    val id: String,
    val index: Int,
    val name: String,
    val type: String,
    val description: String,
)
