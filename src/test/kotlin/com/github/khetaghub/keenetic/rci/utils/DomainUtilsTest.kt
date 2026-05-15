package com.github.khetaghub.keenetic.rci.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DomainUtilsTest {

    @Test
    fun detectsDomainType() {
        assertEquals(DomainType.DOMAIN, DomainUtils.detectType("google.com"))
        assertEquals(DomainType.DOMAIN, DomainUtils.detectType("sub.domain.com"))

        assertEquals(DomainType.IDN, DomainUtils.detectType("пример.рф"))
        assertEquals(DomainType.IDN, DomainUtils.detectType("bücher.de"))

        assertEquals(DomainType.PUNYCODE, DomainUtils.detectType("xn--e1afmkfd.xn--p1ai"))
        assertEquals(DomainType.PUNYCODE, DomainUtils.detectType("xn--bcher-kva.de"))
    }

    @Test
    fun validatesValidDomains() {
        assertTrue(DomainUtils.isValid("google.com"))
        assertTrue(DomainUtils.isValid("sub.domain.com"))
        assertTrue(DomainUtils.isValid("пример.рф"))
        assertTrue(DomainUtils.isValid("bücher.de"))
        assertTrue(DomainUtils.isValid("xn--e1afmkfd.xn--p1ai"))
        assertTrue(DomainUtils.isValid("xn--bcher-kva.de"))
    }

    @Test
    fun rejectsInvalidDomains() {
        assertFalse(DomainUtils.isValid(""))
        assertFalse(DomainUtils.isValid(" "))
        assertFalse(DomainUtils.isValid("bad domain.com"))
        assertFalse(DomainUtils.isValid("domain..com"))
        assertFalse(DomainUtils.isValid("-domain.com"))
        assertFalse(DomainUtils.isValid("domain-.com"))

        assertThrows(IllegalArgumentException::class.java) {
            DomainUtils.detectType("bad domain.com")
        }
    }

    @Test
    fun convertsDomains() {
        assertEquals("xn--e1afmkfd.xn--p1ai", DomainUtils.toAscii("пример.рф"))
        assertEquals("xn--bcher-kva.de", DomainUtils.toAscii("bücher.de"))

        assertEquals("пример.рф", DomainUtils.toUnicode("xn--e1afmkfd.xn--p1ai"))
        assertEquals("bücher.de", DomainUtils.toUnicode("xn--bcher-kva.de"))
    }
}