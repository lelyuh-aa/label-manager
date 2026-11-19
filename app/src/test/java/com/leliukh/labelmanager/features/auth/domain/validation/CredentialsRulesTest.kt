package com.leliukh.labelmanager.features.auth.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialsRulesTest {

    @Test
    fun `emails need a local part, a domain and a top-level domain`() {
        assertTrue(CredentialsRules.isValidEmail("manager@rheinvalley.fm"))
        assertTrue(CredentialsRules.isValidEmail("  manager@rheinvalley.fm  "))
        assertFalse(CredentialsRules.isValidEmail("manager@rheinvalley"))
        assertFalse(CredentialsRules.isValidEmail("manager rheinvalley.fm"))
        assertFalse(CredentialsRules.isValidEmail(""))
    }

    @Test
    fun `a new password needs eight characters, a letter and a digit`() {
        assertEquals(PasswordChecks(false, true, false), check("abc"))
        assertEquals(PasswordChecks(true, true, false), check("backstage"))
        assertEquals(PasswordChecks(true, false, true), check("12345678"))
        assertTrue(check("backstage7").isValid)
    }

    private fun check(password: String) = CredentialsRules.checkNewPassword(password)
}
