package com.github.khetaghub.keenetic.rci.api

/** Interface-related Keenetic operations. */
interface InterfaceApi {

    /** Returns interfaces known to NDMS, ordered as reported by the device. */
    fun getList(): List<Interface>

    /** Deletes interface by name. */
    fun delete(interfaceName: String)

}

data class Interface(
    val id: String,
    val index: Int,
    val name: String,
    val type: String,
    val description: String,
)
