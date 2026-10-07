package com.akrishna87.budgettracker.security

import java.security.MessageDigest
import java.security.SecureRandom

/** Salted SHA-256 so the PIN is never stored in plain text. */
object PinHashing {
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray())
        val hashed = digest.digest(pin.toByteArray())
        return hashed.joinToString("") { "%02x".format(it) }
    }
}
