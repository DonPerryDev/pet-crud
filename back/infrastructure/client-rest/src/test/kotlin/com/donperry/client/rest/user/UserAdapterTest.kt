package com.donperry.client.rest.user

import com.donperry.client.rest.user.dto.UserData
import com.donperry.model.user.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class UserAdapterTest {
    @Mock
    private lateinit var webClient: WebClient

    @Mock
    private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>

    @Mock
    private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>

    @Mock
    private lateinit var responseSpec: WebClient.ResponseSpec

    private lateinit var userAdapter: UserAdapter

    @BeforeEach
    fun setUp() {
        userAdapter = UserAdapter(webClient)
    }

    @Test
    fun `should get user by email successfully`() {
        val email = "test@example.com"
        val userId = UUID.randomUUID()
        val userData = UserData(id = userId, email = email)
        val expectedUser = User(id = userId, email = email)

        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(eq("/api/v1/users/email/{email}"), eq(email)))
            .thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(UserData::class.java)).thenReturn(Mono.just(userData))

        StepVerifier
            .create(userAdapter.getByEmail(email))
            .expectNext(expectedUser)
            .verifyComplete()

        verify(webClient).get()
        verify(requestHeadersUriSpec).uri("/api/v1/users/email/{email}", email)
        verify(requestHeadersSpec).retrieve()
        verify(responseSpec).bodyToMono(UserData::class.java)
    }

    @Test
    fun `should handle webclient error`() {
        val email = "error@example.com"
        val error = RuntimeException("WebClient error")

        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(eq("/api/v1/users/email/{email}"), eq(email)))
            .thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(UserData::class.java)).thenReturn(Mono.error(error))

        StepVerifier
            .create(userAdapter.getByEmail(email))
            .expectError(RuntimeException::class.java)
            .verify()

        verify(webClient).get()
        verify(requestHeadersUriSpec).uri("/api/v1/users/email/{email}", email)
        verify(requestHeadersSpec).retrieve()
        verify(responseSpec).bodyToMono(UserData::class.java)
    }

    @Test
    fun `should handle empty response from webclient`() {
        val email = "empty@example.com"

        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(eq("/api/v1/users/email/{email}"), eq(email)))
            .thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(UserData::class.java)).thenReturn(Mono.empty())

        StepVerifier
            .create(userAdapter.getByEmail(email))
            .verifyComplete()

        verify(webClient).get()
        verify(requestHeadersUriSpec).uri("/api/v1/users/email/{email}", email)
        verify(requestHeadersSpec).retrieve()
        verify(responseSpec).bodyToMono(UserData::class.java)
    }
}
