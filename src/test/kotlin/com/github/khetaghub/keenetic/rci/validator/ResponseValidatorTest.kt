package com.github.khetaghub.keenetic.rci.validator

import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.command.RciCommandType
import com.github.khetaghub.keenetic.rci.exception.KeeneticNdmsException
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResponseValidatorTest {

    private val validator: ResponseValidator = DefaultResponseValidator()

    @Test
    fun validateHttpResponse_invalidJson() {
        val response = "invalidJson"
        assertThrows<KeeneticNdmsException> {
            validator.validate(RciCommandType.HTTP, mockk<RciCommand<*>>(), response)
        }
    }

    @Test
    fun validateHttpResponse_oneError() {
        val response = """
            [
              {
                "dns-proxy": {
                  "route": {
                    "status": [
                      {
                        "status": "error",
                        "code": "4457148",
                        "ident": "Dns::Route::Manager",
                        "message": "unable to find a route to \"bad-route\"."
                      }
                    ]
                  }
                }
              }
            ]
        """.trimIndent()

        assertThrows<KeeneticNdmsException>(
            """
            NDMS returned 2 error(s):
            1. [Dns::Route::Manager] code=4457148: unable to find a route to "bad-route".
        """.trimIndent()
        ) {
            validator.validate(RciCommandType.HTTP, mockk<RciCommand<*>>(), response)
        }
    }

    @Test
    fun validateHttpResponse_manyErrors() {
        val response = """
            [
              {
                "bad-command": {
                  "status": [
                    {
                      "status": "error",
                      "code": "1179781",
                      "ident": "Core::Configurator",
                      "message": "not found: \"bad-command\" [http/rci]."
                    }
                  ]
                }
              },
              {
                "dns-proxy": {
                  "route": {
                    "status": [
                      {
                        "status": "error",
                        "code": "4457148",
                        "ident": "Dns::Route::Manager",
                        "message": "unable to find a route to \"bad-route\"."
                      }
                    ]
                  }
                }
              }
            ]
        """.trimIndent()

        assertThrows<KeeneticNdmsException>(
            """
            NDMS returned 2 error(s):
            1. [Core::Configurator] code=1179781: not found: "bad-command" [http/rci].
            2. [Dns::Route::Manager] code=4457148: unable to find a route to "bad-route".
        """.trimIndent()
        ) {
            validator.validate(RciCommandType.HTTP, mockk<RciCommand<*>>(), response)
        }
    }

    @Test
    fun validateCliResponse() {
        val cases = listOf(
            Pair(
                "Command::Base error[7405600]: message",
                """
                    NDMS returned 1 error(s):
                    1. [Command::Base] code=7405600: message
                """.trimIndent()
            ),
            Pair(
                "Command::Base error[7405600]: any message",
                """
                    NDMS returned 1 error(s):
                    1. [Command::Base] code=7405600: any message
                """.trimIndent()
            ),
            Pair(
                "Command::Base error[42]:",
                """
                    NDMS returned 1 error(s):
                    1. [Command::Base] code=42:
                """.trimIndent()
            ),
            Pair(
                "Command::Base error[42]",
                """
                    NDMS returned 1 error(s):
                    1. [Command::Base] code=42
                """.trimIndent()
            ),
            Pair(
                "Configurator error[852002]: address: argument parse error.",
                """
                    NDMS returned 1 error(s):
                    1. [Configurator] code=852002: address: argument parse error.
                """.trimIndent()
            ),
            Pair(
                "lib::libndmComponents error[268369922]: updates are available for this system.",
                """
                    NDMS returned 1 error(s):
                    1. [lib::libndmComponents] code=268369922: updates are available for this system.
                """.trimIndent()
            ),
            Pair(
                "Mobile::Interface error[73140786]: \"UsbLte0\": timeout waiting ► for expected response.",
                """
                    NDMS returned 1 error(s):
                    1. [Mobile::Interface] code=73140786: "UsbLte0": timeout waiting ► for expected response.
                """.trimIndent()
            ),
        )

        val command = mockk<RciCommand<*>>()
        for (case in cases) {
            assertThrows<KeeneticNdmsException>(case.second) {
                validator.validate(RciCommandType.CLI, command, case.first)
            }
        }
    }

}