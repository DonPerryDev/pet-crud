package com.donperry.model.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class UserTest {
    @Test
    fun `should create user with id and email`() {
        val id = UUID.randomUUID()
        val email = "test@example.com"

        val user = User(id = id, email = email)

        assertEquals(id, user.id)
        assertEquals(email, user.email)
    }

    @Test
    fun `should support data class equality`() {
        val id = UUID.randomUUID()
        val email = "test@example.com"

        val user1 = User(id = id, email = email)
        val user2 = User(id = id, email = email)

        assertEquals(user1, user2)
    }

    @Test
    fun `should support data class copy`() {
        val id = UUID.randomUUID()
        val email = "test@example.com"
        val newEmail = "newemail@example.com"

        val user = User(id = id, email = email)
        val copiedUser = user.copy(email = newEmail)

        assertEquals(id, copiedUser.id)
        assertEquals(newEmail, copiedUser.email)
        assertNotEquals(user, copiedUser)
    }

    @Test
    fun `should have proper toString representation`() {
        val id = UUID.randomUUID()
        val email = "test@example.com"

        val user = User(id = id, email = email)
        val toString = user.toString()

        assertTrue(toString.contains("User"))
        assertTrue(toString.contains(id.toString()))
        assertTrue(toString.contains(email))
    }

    @Test
    fun `should have proper hashCode implementation`() {
        val id = UUID.randomUUID()
        val email = "test@example.com"

        val user1 = User(id = id, email = email)
        val user2 = User(id = id, email = email)

        assertEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun `should handle different UUIDs as different users`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val email = "test@example.com"

        val user1 = User(id = id1, email = email)
        val user2 = User(id = id2, email = email)

        assertNotEquals(user1, user2)
        assertNotEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun `should handle different emails as different users`() {
        val id = UUID.randomUUID()
        val email1 = "test1@example.com"
        val email2 = "test2@example.com"

        val user1 = User(id = id, email = email1)
        val user2 = User(id = id, email = email2)

        assertNotEquals(user1, user2)
    }
}
