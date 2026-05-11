package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.command.RciCommand

/** Transport capable of executing an SDK command against a Keenetic device. */
interface KeeneticTransport {

    /** Sends the command using the concrete transport and returns the raw device response. */
    fun execute(command: RciCommand<*>): String

}
