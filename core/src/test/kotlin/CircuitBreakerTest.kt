import com.github.huymaster.server.core.utils.CircuitBreaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class CircuitBreakerTest {
    private fun createTestBreaker(): CircuitBreaker = CircuitBreaker.invoke {
        minimumNumberOfCalls = 5
        slidingWindowSize = 10
        failureRateThreshold = 0.5
        waitDurationInOpenState = Duration.ofMillis(100)
        permittedCallsInHalfOpenState = 2
    }

    @Test
    fun case1() = runBlocking {
        val breaker = createTestBreaker()
        assertEquals(CircuitBreaker.State.CLOSED, breaker.state)

        repeat(10) {
            val result = breaker.execute { "SUCCESS" }
            assertEquals("SUCCESS", result)
        }

        assertEquals(CircuitBreaker.State.CLOSED, breaker.state)
    }

    @Test
    fun case2() = runBlocking {
        val breaker = createTestBreaker()

        repeat(4) {
            assertFailsWith<RuntimeException> {
                breaker.execute { throw RuntimeException("Simulated Error") }
            }
        }

        assertEquals(CircuitBreaker.State.CLOSED, breaker.state)
    }

    @Test
    fun case3() = runBlocking {
        val breaker = createTestBreaker()

        repeat(2) { breaker.execute { "SUCCESS" } }
        repeat(3) {
            assertFailsWith<RuntimeException> {
                breaker.execute { throw RuntimeException("Simulated Error") }
            }
        }

        assertEquals(CircuitBreaker.State.OPEN, breaker.state)

        val exception = assertFailsWith<CircuitBreaker.CircuitBreakerException> {
            breaker.execute { "THIS SHOULD NOT RUN" }
        }
        assertTrue(exception.message!!.contains("Circuit is OPEN"))
    }

    @Test
    fun case4() = runBlocking {
        val breaker = createTestBreaker()

        repeat(5) {
            assertFailsWith<RuntimeException> { breaker.execute { throw RuntimeException("Error") } }
        }
        assertEquals(CircuitBreaker.State.OPEN, breaker.state)

        delay(150.milliseconds)

        val result = breaker.execute { "TRIAL_SUCCESS" }
        assertEquals("TRIAL_SUCCESS", result)
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.state)
    }

    @Test
    fun case5() = runBlocking {
        val breaker = createTestBreaker()

        repeat(5) {
            assertFailsWith<RuntimeException> { breaker.execute { throw RuntimeException("Error") } }
        }

        delay(150.milliseconds)

        breaker.execute { "TRIAL_1" }
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.state)

        breaker.execute { "TRIAL_2" }

        assertEquals(CircuitBreaker.State.CLOSED, breaker.state)
    }

    @Test
    fun case6() = runBlocking {
        val breaker = createTestBreaker()

        repeat(5) {
            assertFailsWith<RuntimeException> { breaker.execute { throw RuntimeException("Error") } }
        }

        delay(150.milliseconds)

        assertFailsWith<RuntimeException> {
            breaker.execute { throw RuntimeException("Trial Failed") }
        }

        assertEquals(CircuitBreaker.State.OPEN, breaker.state)
    }

    @Test
    fun case7() = runBlocking {
        val breaker = createTestBreaker()
        repeat(6) { breaker.execute { "SUCCESS" } }
        repeat(4) {
            assertFailsWith<RuntimeException> { breaker.execute { throw RuntimeException("Error") } }
        }
        assertEquals(CircuitBreaker.State.CLOSED, breaker.state)

        assertFailsWith<RuntimeException> { breaker.execute { throw RuntimeException("Error") } }
        assertEquals(CircuitBreaker.State.OPEN, breaker.state)
    }
}