package main

import kotlin.random.Random

fun <A> choose(vararg options: A): A = options[Random.nextInt(0, options.size)]
fun <A> maybe(b:() -> A): A? = Random.nextInt(0, 2).takeIf { it != 0 }?.let { b() }
fun <A> maybeChoose(vararg options: A): A? = maybe { options.takeIf { it.isNotEmpty() }?.let { choose(*options) } }
fun <A> List<A>.rand(): A = get(Random.nextInt(0, size))

val names = listOf("Hue Raffaele", "Adam Halsey", "Katharyn Duhon", "Jacqueline Betters", "Klara Melchior")
val streetNames = listOf("Duane street", "Jordon street", "Wes avenue", "Raymon road", "Joshua street")
val cities = listOf("Poprad", "Zilina", "New York", "London", "Hague")

fun generateUserRand(): User = User(
        name = names[Random.nextInt(0, names.size)],
        age = maybe { Random.nextInt(1, 30) },
        address = Address(
                city = City(
                        name = cities[Random.nextInt(0, cities.size)],
                        population = Random.nextInt(100, 100000)
                ),
                street = maybe {
                    Street(
                            name = streetNames[Random.nextInt(0, streetNames.size)],
                            number = Random.nextInt(0, 15)
                    )
                }
        ),
        gender = maybeChoose(Gender.MALE, Gender.FEMALE)
)

fun generateUser(): User = User(
        name = names[Random.nextInt(0, names.size)],
        age = Random.nextInt(1, 30),
        address = Address(
                city = City(
                        name = cities[Random.nextInt(0, cities.size)],
                        population = Random.nextInt(100, 100000)
                ),
                street = Street(
                    name = streetNames[Random.nextInt(0, streetNames.size)],
                    number = Random.nextInt(0, 15)
                )
        ),
        gender = choose(Gender.MALE, Gender.FEMALE)
)

fun generateUserNull(): User = generateUser().let { user ->
    var new = UserZoom.address.street.set(user, null)
    new = UserZoom.age.set(new, null)
    new = UserZoom.gender.set(new, null)
    new
}

fun generateXUsers(x: Int) = (1..x).map { generateUserRand() }.let { it.toMutableList() } .let { users ->
    if (users.all { it.age == null }) {
        val user = users.removeAt(Random.nextInt(0, users.size))
        users.add(UserZoom.age.set(user, Random.nextInt(1, 30)))
    }

    if (users.all { it.address.street == null }) {
        val user = users.removeAt(Random.nextInt(0, users.size))
        users.add(UserZoom.address.street.set(user, Street(streetNames[Random.nextInt(0, streetNames.size)], Random.nextInt(1, 15))))
    }

    if (users.all { it.gender == null }) {
        val user = users.removeAt(Random.nextInt(0, users.size))
        users.add(UserZoom.gender.set(user, choose(Gender.MALE, Gender.FEMALE)))
    }
    users
}

