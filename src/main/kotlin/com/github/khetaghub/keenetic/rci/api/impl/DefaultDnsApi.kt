package com.github.khetaghub.keenetic.rci.api.impl

import com.github.khetaghub.keenetic.rci.api.*
import com.github.khetaghub.keenetic.rci.command.AddDnsOverHttpsCommand
import com.github.khetaghub.keenetic.rci.command.AddDnsOverTlsCommand
import com.github.khetaghub.keenetic.rci.command.AddPlainDnsCommand
import com.github.khetaghub.keenetic.rci.command.DeleteDnsOverHttpsCommand
import com.github.khetaghub.keenetic.rci.command.DeleteDnsOverTlsCommand
import com.github.khetaghub.keenetic.rci.command.DeletePlainDnsCommand
import com.github.khetaghub.keenetic.rci.command.GetPlainDnsCommand
import com.github.khetaghub.keenetic.rci.command.GetProviderDnsCommand
import com.github.khetaghub.keenetic.rci.command.GetSecuredDnsCommand
import com.github.khetaghub.keenetic.rci.command.SetProviderDnsCommand

internal class DefaultDnsApi(
    private val executor: RciCommandExecutor,
) : DnsApi {

    override fun getList(): List<Dns> {
        val plainDns = getPlainList()
        val securedDns = getSecuredList()
        val dns = plainDns + securedDns
        return dns
    }

    override fun getPlainList(): List<PlainDns> {
        return executor.execute(GetPlainDnsCommand())
    }

    override fun getSecuredList(): List<SecuredDns> {
        return executor.execute(GetSecuredDnsCommand())
    }

    override fun add(plainDns: PlainDns) {
        executor.executeWithoutResponse(AddPlainDnsCommand(plainDns))
    }

    override fun add(dnsOverTls: DnsOverTls) {
        executor.executeWithoutResponse(AddDnsOverTlsCommand(dnsOverTls))
    }

    override fun add(dnsOverHttps: DnsOverHttps) {
        executor.executeWithoutResponse(AddDnsOverHttpsCommand(dnsOverHttps))
    }

    override fun deletePlainByAddress(address: String) {
        executor.executeWithoutResponse(DeletePlainDnsCommand(address))
    }

    override fun deleteAllPlain() {
        getPlainList()
            .map { it.address }
            .distinct()
            .forEach(::deletePlainByAddress)
    }

    override fun deleteDnsOverTlsByAddress(address: String) {
        executor.executeWithoutResponse(DeleteDnsOverTlsCommand(address))
    }

    override fun deleteAllDnsOverTls() {
        getSecuredList()
            .filterIsInstance<DnsOverTls>()
            .map { it.address }
            .distinct()
            .forEach(::deleteDnsOverTlsByAddress)
    }

    override fun deleteDnsOverHttpsByAddress(address: String) {
        executor.executeWithoutResponse(DeleteDnsOverHttpsCommand(address))
    }

    override fun deleteAllDnsOverHttps() {
        getSecuredList()
            .filterIsInstance<DnsOverHttps>()
            .map { it.address }
            .distinct()
            .forEach(::deleteDnsOverHttpsByAddress)
    }

    override fun deleteAllSecured() {
        val securedDns = getSecuredList()

        securedDns
            .filterIsInstance<DnsOverTls>()
            .map { it.address }
            .distinct()
            .forEach(::deleteDnsOverTlsByAddress)

        securedDns
            .filterIsInstance<DnsOverHttps>()
            .map { it.address }
            .distinct()
            .forEach(::deleteDnsOverHttpsByAddress)
    }

    override fun deleteAll() {
        deleteAllPlain()
        deleteAllSecured()
    }

    override fun getProviderDns(interfaceName: String): ProviderDnsState {
        return executor.execute(GetProviderDnsCommand(interfaceName))
    }

    override fun setProviderDns(interfaceName: String, ipVersion: ProviderDnsIpVersion, enabled: Boolean) {
        executor.executeWithoutResponse(SetProviderDnsCommand(interfaceName, ipVersion, enabled))
    }

}
