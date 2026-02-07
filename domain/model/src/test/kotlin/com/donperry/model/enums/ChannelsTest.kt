package com.donperry.model.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChannelsTest {
    @Test
    fun `should have EMAIL channel`() {
        val channel = Channels.EMAIL

        assertEquals("EMAIL", channel.name)
        assertEquals(Channels.EMAIL, channel)
    }

    @Test
    fun `should have WEB_SSE channel`() {
        val channel = Channels.WEB_SSE

        assertEquals("WEB_SSE", channel.name)
        assertEquals(Channels.WEB_SSE, channel)
    }

    @Test
    fun `should have exactly two channels`() {
        val channels = Channels.values()

        assertEquals(2, channels.size)
        assertTrue(channels.contains(Channels.EMAIL))
        assertTrue(channels.contains(Channels.WEB_SSE))
    }

    @Test
    fun `should support enum equality`() {
        val channel1 = Channels.EMAIL
        val channel2 = Channels.EMAIL

        assertEquals(channel1, channel2)
    }

    @Test
    fun `should differentiate between channels`() {
        val emailChannel = Channels.EMAIL
        val webSSEChannel = Channels.WEB_SSE

        assertNotEquals(emailChannel, webSSEChannel)
    }

    @Test
    fun `should support valueOf`() {
        val emailChannel = Channels.valueOf("EMAIL")
        val webSSEChannel = Channels.valueOf("WEB_SSE")

        assertEquals(Channels.EMAIL, emailChannel)
        assertEquals(Channels.WEB_SSE, webSSEChannel)
    }

    @Test
    fun `should have proper toString representation`() {
        val emailToString = Channels.EMAIL.toString()
        val webSSEToString = Channels.WEB_SSE.toString()

        assertEquals("EMAIL", emailToString)
        assertEquals("WEB_SSE", webSSEToString)
    }

    @Test
    fun `should have consistent ordinal values`() {
        assertEquals(0, Channels.EMAIL.ordinal)
        assertEquals(1, Channels.WEB_SSE.ordinal)
    }
}
