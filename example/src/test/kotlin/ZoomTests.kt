import main.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import zoom.Lens
import zoom.at
import zoom.first
import zoom.index
import kotlin.random.Random


@DisplayName("Zoom tests")
class ZoomTests {

    @Nested
    @DisplayName("Simple class test cases")
    inner class SimpleCaseTest {
        private val user: User = generateUser()
        private val userNull: User = generateUserNull()

        @Nested
        @DisplayName("when get")
        inner class Get {
            @TestFactory
            fun `lens should always return non null value`(): Collection<DynamicTest> {
                val lens = listOf<Lens<User, *>>(UserZoom.name, UserZoom.address, UserZoom.address.city, UserZoom.address.city.name, UserZoom.address.city.population)
                return lens.map { l ->
                    DynamicTest.dynamicTest("Lens<User, *>.get(User) not null") {
                        assertNotNull(l.get(user))
                    }
                }
            }

            @TestFactory
            fun `optional may return null value`(): Collection<DynamicTest> {
                val optional = listOf<Lens<User, *>>(UserZoom.age, UserZoom.gender, UserZoom.address.street)
                return optional.map { o ->
                    DynamicTest.dynamicTest("Optional<User, *>.get(User) can be null") {
                        assertTrue(o.get(userNull) != null || o.get(userNull) == null)
                    }
                }
            }
        }

        @Nested
        @DisplayName("when set")
        inner class Set {
            @Test
            fun `Lens set primitive value should return copy of with updated primitive value`() {
                val lens = UserZoom.name
                val oldName = lens.get(user)
                val newUser = lens.set(user, "Pamela")
                assertEquals("Pamela", newUser.name)
                assertNotEquals(oldName, newUser.name)
            }

            @Test
            fun `Lens set class value should return copy of with updated class value`() {
                val lens = UserZoom.address
                val newAddress = generateUser().address
                val oldAddress = lens.get(user)
                val newUser = lens.set(user, newAddress)

                assertEquals(newAddress, newUser.address)
                assertNotEquals(oldAddress, newUser.address)
            }

            @Test
            fun `Optional set primitive value should return copy with updated primitive value`() {
                assumeTrue(userNull.age == null, "userNull should have null value for Optional")
                val newUser = UserZoom.age.set(userNull, 10)
                assertNotNull(newUser.age)
            }

            @Test
            fun `Optional set class value should return copy with updated class value`() {
                assumeTrue(userNull.address.street == null, "userNull should have null value for Optional")
                val newStreet = generateUser().address.street
                assumeTrue(newStreet != null, "generateUser should not contain null values")
                val newUser = UserZoom.address.street.set(userNull, newStreet)
                assertNotNull(newUser.address.street)
            }
        }
    }

    @Nested
    @DisplayName("List test cases")
    inner class ListCaseTest {
        private val state = State(generateXUsers(10) + listOf(generateUser().copy(name = "Tomas")))

        @Nested
        @DisplayName("when get")
        inner class Get {
            @Test
            fun `lens list elements index should return expected element`() {
                val index = 5
                assumeTrue(state.usersList.size > index + 1, "state userList is shorter than testing index")
                val expected = state.usersList[index]
                val result = MyZoom.usersList.index(index).get(state)
                assertEquals(expected, result)
            }

            @Test
            fun `lens list elements index may return null if accessing invalid position`() {
                val index = state.usersList.size
                assertNull(MyZoom.usersList.index(index).get(state))
            }

            @Test
            fun `lens list elements should return expected if predicate matches`() {
                val predicate: (User) -> Boolean = { it.name == "Tomas" }
                assertEquals(state.usersList.find(predicate), MyZoom.usersList.first(predicate).get(state))
            }

            @Test
            fun `lens list elements should return null if predicate not matches`() {
                val predicate: (User) -> Boolean = { it.name == "Jozko" }
                assertNull(MyZoom.usersList.first(predicate).get(state))
            }
        }

        @Nested
        @DisplayName("when set")
        inner class Set {
            private var dynamicState = State(generateXUsers(5) + listOf(generateUserNull()))

            @BeforeEach
            fun beforeEach() {
                dynamicState = State(generateXUsers(5) + listOf(generateUserNull().copy(name = "Marek")))
            }

            @Test
            fun `lens list elements set index should return updated copy`() {
                val index = Random.nextInt(0, dynamicState.usersList.size)
                val indexRef = MyZoom.usersList.index(index)

                dynamicState = indexRef.name.set(dynamicState, "Jozko")
                assertEquals("Jozko", indexRef.name.get(dynamicState))

                dynamicState = indexRef.age.set(dynamicState, 99)
                assertEquals(99, indexRef.age(dynamicState))

                dynamicState = indexRef.address.city.name.set(dynamicState, "Krakow")
                assertEquals("Krakow", indexRef.address.city.name.get(dynamicState))

                dynamicState = indexRef.address.street.set(dynamicState, Street("Aloho mora", 666))
                assertEquals(Street("Aloho mora", 666), indexRef.address.street.get(dynamicState))
            }

            @Test
            fun `lens list elements set index should return same state if out of bounds`() {
                val index = dynamicState.usersList.size
                val indexRef = MyZoom.usersList.index(index)

                assertEquals(dynamicState, indexRef.name.set(dynamicState, "Jozko"))
                assertEquals(dynamicState, indexRef.age.set(dynamicState, 99))
                assertEquals(dynamicState, indexRef.address.city.name.set(dynamicState, "Krakow"))
                assertEquals(dynamicState, indexRef.address.street.set(dynamicState, Street("Aloho mora", 666)))
            }

            @Test
            fun `lens list elements set predicate should return updated copy if matched`() {
                val predicate: (User) -> Boolean = { it.name == "Marek" }
                val predicateRef = MyZoom.usersList.first(predicate)

                dynamicState = predicateRef.name.set(dynamicState, "Jozko")
                assertEquals("Jozko", predicateRef.name.get(dynamicState))

                dynamicState = predicateRef.age.set(dynamicState, 99)
                assertEquals(99, predicateRef.age(dynamicState))

                dynamicState = predicateRef.address.city.name.set(dynamicState, "Krakow")
                assertEquals("Krakow", predicateRef.address.city.name.get(dynamicState))

                dynamicState = predicateRef.address.street.set(dynamicState, Street("Aloho mora", 666))
                assertEquals(Street("Aloho mora", 666), predicateRef.address.street.get(dynamicState))
            }

            @Test
            fun `lens list elements set predicate should return null if not matched`() {
                val predicate: (User) -> Boolean = { it.name == "Trump" }
                val predicateRef = MyZoom.usersList.first(predicate)

                assertEquals(dynamicState, predicateRef.name.set(dynamicState, "Jozko"))
                assertEquals(dynamicState, predicateRef.age.set(dynamicState, 99))
                assertEquals(dynamicState, predicateRef.address.city.name.set(dynamicState, "Krakow"))
                assertEquals(dynamicState, predicateRef.address.street.set(dynamicState, Street("Aloho mora", 666)))
            }
        }
    }

    @Nested
    @DisplayName("Map test cases")
    inner class MapCaseTest {
        private val state = State(usersMap = generateXUsers(10).associateBy { it.name })

        @Nested
        @DisplayName("when get")
        inner class Get {
            @Test
            fun `lens map of elements get at should return expected`() {
                val key = state.usersMap.keys.toList()[Random.nextInt(0, state.usersMap.keys.size)]
                val expected = state.usersMap[key]
                val atRef = MyZoom.usersMap.at(key)

                assertEquals(expected, atRef.get(state))
            }

            @TestFactory
            fun `lens map of elements get at should return null if not exists`(): Collection<DynamicTest> =
                names.map { name ->
                    if (state.usersMap[name] == null)
                        DynamicTest.dynamicTest("Key $name = null") {
                            assertNull(MyZoom.usersMap.at(name).get(state))
                        }
                    else
                        DynamicTest.dynamicTest("Key $name = Value") {
                            assertEquals(state.usersMap[name], MyZoom.usersMap.at(name).get(state))
                        }
                }

            @TestFactory
            fun `lens map of elements get at should return null if not exists and set`(): Collection<DynamicTest> =
                    names.map { name ->
                        if (state.usersMap[name] == null)
                            DynamicTest.dynamicTest("Key $name = null") {
                                assertNull(MyZoom.usersMap.at(name).get(state))
                            }
                        else
                            DynamicTest.dynamicTest("Key $name = Value") {
                                assertEquals(state.usersMap[name], MyZoom.usersMap.at(name).get(state))
                            }
                    }
        }

        @Nested
        @DisplayName("when set")
        inner class Set {
            private var dynamicState = State(generateXUsers(10))

            @BeforeEach
            fun beforeEach() {
                dynamicState = State(generateXUsers(10))
            }

            @Test
            fun `lens map of elements set at should return expected`() {
                val key = dynamicState.usersMap.keys.toList()[Random.nextInt(0, dynamicState.usersMap.keys.size)]
                val atRef = MyZoom.usersMap.at(key)

                dynamicState = atRef.name.set(dynamicState, "Jozko")
                assertEquals("Jozko", atRef.name.get(dynamicState))

                dynamicState = atRef.age.set(dynamicState, 99)
                assertEquals(99, atRef.age(dynamicState))

                dynamicState = atRef.address.city.name.set(dynamicState, "Krakow")
                assertEquals("Krakow", atRef.address.city.name.get(dynamicState))

                dynamicState = atRef.address.street.set(dynamicState, Street("Aloho mora", 666))
                assertEquals(Street("Aloho mora", 666), atRef.address.street.get(dynamicState))
            }

            @Test
            fun `lens map of elements set at should return same state if not exists`() {
                val key = "Charlie"
                val atRef = MyZoom.usersMap.at(key)

                assertEquals(dynamicState, atRef.name.set(dynamicState, "Jozko"))
                assertEquals(dynamicState, atRef.age.set(dynamicState, 99))
                assertEquals(dynamicState, atRef.address.city.name.set(dynamicState, "Krakow"))
                assertEquals(dynamicState, atRef.address.street.set(dynamicState, Street("Aloho mora", 666)))
            }
        }
    }
}