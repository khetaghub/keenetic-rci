package com.github.khetaghub.keenetic.rci.api

import com.github.khetaghub.keenetic.rci.command.RciCommand

interface KeeneticTransport {

    fun execute(command: RciCommand<*>): String

}