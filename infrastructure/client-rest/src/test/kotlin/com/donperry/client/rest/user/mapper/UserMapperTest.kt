package com.donperry.client.rest.user.mapper

import com.donperry.client.rest.user.dto.UserData
import com.donperry.model.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class UserMapperTest {
    @Test
    fun `should map User to UserData`() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val user = User(id = userId, email = email)

        val result = UserMapper.toUserData(user)

        assertEquals(userId, result.id)
        assertEquals(email, result.email)
    }

    @Test
    fun `should map UserData to User`() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val userData = UserData(id = userId, email = email)

        val result = UserMapper.toUser(userData)

        assertEquals(userId, result.id)
        assertEquals(email, result.email)
    }
}
