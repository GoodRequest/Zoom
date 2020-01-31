package main

import zoom.*


@Zoom data class User(
    val name: String,
    val age: Int,
    val address: Address)

@Zoom data class Address(
    val city: String,
    val street: Street?)

@Zoom data class Street(
    val name: String,
    val number: Int)



fun main() {

    val user = User(
        name = "Johnny",
        age = 43,
        address = Address(
            city = "The Vadicov",
            street = Street(
                name = "Old",
                number = 1
            )))


    val streetName = UserZoom.address.street.name
    val updatedUser = streetName.set(user, "New")

    print(updatedUser)
}