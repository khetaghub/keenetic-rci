package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.command.RciCommand

/** Transport capable of executing an SDK command against a Keenetic device. */
interface KeeneticTransport {

    /** Sends the command using the concrete transport and returns the raw device response. */
    fun execute(command: RciCommand<*>): String

    /**
     * Sends a transport-specific raw command and returns the raw device response without parsing.
     *
     * Implementations interpret [rawCommand] according to the selected transport:
     * [com.github.khetaghub.keenetic.rci.transport.HttpTransport] expects a JSON RCI payload,
     * while [com.github.khetaghub.keenetic.rci.transport.SshTransport] expects a CLI command line.
     */
    fun execute(rawCommand: String): String

}
