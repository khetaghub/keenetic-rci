package com.github.khetaghub.keenetic.rci.transport.ssh

import com.github.khetaghub.keenetic.rci.api.KeeneticTransport
import com.github.khetaghub.keenetic.rci.command.CliCommand
import com.github.khetaghub.keenetic.rci.command.CliCommandView
import com.github.khetaghub.keenetic.rci.command.RciCommand
import com.github.khetaghub.keenetic.rci.exception.CliCommandExecutionException
import com.github.khetaghub.keenetic.rci.exception.KeeneticRciException
import mu.KotlinLogging
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.File
import java.util.concurrent.TimeUnit

class SshTransport private constructor(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val connectTimeoutMillis: Int,
    private val commandTimeoutMillis: Long,
    private val knownHostsFile: File?,
    private val allowAnyHostKey: Boolean,
    private val hostKeyVerifier: HostKeyVerifier?,
) : KeeneticTransport {

    private val ansiRegex = Regex("\\u001B\\[[;\\d]*[ -/]*[@-~]")
    private val logger = KotlinLogging.logger { }

    override fun execute(command: RciCommand<*>): String {
        val cliCommand = command as? CliCommand<*>
            ?: throw KeeneticRciException("SSH transport supports only CLI commands")

        return SSHClient().use { ssh ->
            configureHostKeyVerification(ssh)

            ssh.connectTimeout = connectTimeoutMillis
            ssh.timeout = commandTimeoutMillis.toInt()

            ssh.connect(host, port)
            ssh.authPassword(username, password)

            when (val view = cliCommand.cliCommand) {
                is CliCommandView.Single ->
                    ssh.startSession().use { session ->
                        executeCommand(session, view.command)
                    }

                is CliCommandView.Sequential -> {
                    if (view.commands.isEmpty()) throw KeeneticRciException("CLI sequential must not be empty")

                    view.commands.joinToString("\n") { commandLine ->
                        ssh.startSession().use { session ->
                            executeCommand(session, commandLine)
                        }
                    }
                }
            }
        }
    }

    private fun executeCommand(session: Session, cliCommand: String): String {
        val command = session.exec(cliCommand)
        command.join(commandTimeoutMillis, TimeUnit.MILLISECONDS)

        val stdout = command.inputStream.bufferedReader().use { it.readText() }
        val exitStatus = command.exitStatus

        if (exitStatus != null && exitStatus != 0) {
            throw CliCommandExecutionException(
                exitCode = exitStatus,
                command = cliCommand,
                message = stdout.replace(ansiRegex, "").trim()
            )
        }

        logger.debug { "command=${command.javaClass.simpleName} response=$stdout" }

        return stdout.trim()
    }

    private fun configureHostKeyVerification(ssh: SSHClient) {
        when {
            hostKeyVerifier != null -> ssh.addHostKeyVerifier(hostKeyVerifier)
            allowAnyHostKey -> ssh.addHostKeyVerifier(PromiscuousVerifier())
            knownHostsFile != null -> ssh.loadKnownHosts(knownHostsFile)
            else -> {
                val defaultKnownHosts = File(System.getProperty("user.home"), ".ssh/known_hosts")
                if (!defaultKnownHosts.exists()) {
                    "Known hosts file not found at ${defaultKnownHosts.absolutePath}. " +
                            "Use knownHostsFile(...) or allowAnyHostKey() explicitly."
                }
                ssh.loadKnownHosts(defaultKnownHosts)
            }
        }
    }

    class Builder {
        private var host: String? = null
        private var port: Int = DEFAULT_PORT
        private var username: String? = null
        private var password: String? = null
        private var connectTimeoutMillis: Int = DEFAULT_CONNECT_TIMEOUT_MILLIS
        private var commandTimeoutMillis: Long = DEFAULT_COMMAND_TIMEOUT_MILLIS
        private var knownHostsFile: File? = null
        private var allowAnyHostKey: Boolean = false
        private var hostKeyVerifier: HostKeyVerifier? = null

        fun host(host: String) = apply {
            this.host = host
        }

        fun port(port: Int) = apply {
            this.port = port
        }

        fun credentials(username: String, password: String) = apply {
            this.username = username
            this.password = password
        }

        fun connectTimeoutMillis(connectTimeoutMillis: Int) = apply {
            this.connectTimeoutMillis = connectTimeoutMillis
        }

        fun commandTimeoutMillis(commandTimeoutMillis: Long) = apply {
            this.commandTimeoutMillis = commandTimeoutMillis
        }

        fun knownHostsFile(file: File) = apply {
            this.knownHostsFile = file
            this.allowAnyHostKey = false
            this.hostKeyVerifier = null
        }

        fun knownHostsFile(path: String) = apply {
            knownHostsFile(File(path))
        }

        fun hostKeyVerifier(hostKeyVerifier: HostKeyVerifier) = apply {
            this.hostKeyVerifier = hostKeyVerifier
            this.allowAnyHostKey = false
        }

        fun allowAnyHostKey() = apply {
            this.allowAnyHostKey = true
            this.knownHostsFile = null
            this.hostKeyVerifier = null
        }

        fun build(): SshTransport = SshTransport(
            host = requireNotNull(host) { "host is required" },
            port = port,
            username = requireNotNull(username) { "username is required" },
            password = requireNotNull(password) { "password is required" },
            connectTimeoutMillis = connectTimeoutMillis,
            commandTimeoutMillis = commandTimeoutMillis,
            knownHostsFile = knownHostsFile,
            allowAnyHostKey = allowAnyHostKey,
            hostKeyVerifier = hostKeyVerifier,
        )
    }

    companion object {
        private const val DEFAULT_PORT = 22
        private const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 5_000
        private const val DEFAULT_COMMAND_TIMEOUT_MILLIS = 10_000L

        fun builder(): Builder = Builder()
    }
}
