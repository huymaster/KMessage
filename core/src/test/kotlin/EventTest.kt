import com.github.huymaster.server.api.utils.ReflectionTypeHierarchyResolver
import kotlinx.coroutines.runBlocking
import javax.swing.JFrame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTimedValue

class EventTest {
    @Test
    fun test() = runBlocking {
        val resolver = ReflectionTypeHierarchyResolver()
        val st = measureTimedValue {
            resolver.getAllSupertypes(JFrame::class)
        }
        val nd = measureTimedValue {
            resolver.getAllSupertypes(JFrame::class)
        }
        val rd = measureTimedValue {
            resolver.getAllSupertypes(JFrame::class)
        }

        println("Time: ${st.duration} ${nd.duration} ${rd.duration}")
        assertEquals(st.value, nd.value)
        assertEquals(nd.value, rd.value)
    }
}