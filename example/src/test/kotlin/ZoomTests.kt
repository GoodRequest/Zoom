import main.User
import main.generateUser
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


@DisplayName("Zoom usage with simple class")
class SimpleCaseTest {
    val user: User = generateUser()

    @Test
    fun assertMe() {
        assertTrue(true)
    }
}