package main

import zoom.*


@Zoom data class User(
    val name    : String,
    val age     : Int,
    val address : Address)

@Zoom data class Address(
    val city    : String,
    val street  : Street,
    val streets : Map<Int, Street>)

@Zoom data class Street(
    val name   : String?,
    val number : Int)



fun main() {

    val user = User(
        name = "Johnny",
        age = 43,
        address = Address(
            city = "The Vadicov",
            streets = mapOf(
                1 to Street(
                    name = "Old",
                    number = 1
                )
            ),
            street = Street(
                name = "Old",
                number = 1
            )))


    val a : Lens<User, String?> = UserZoom.address.street.name
    val b : Lens<User, Int>     = UserZoom.address.street.number

    val x: Optional<User, String?> = UserZoom.address.streets.at(1).name
    val y: Optional<User, Int>     = UserZoom.address.streets.at(1).number

    val updatedUser = a.set(user, null)

    print(updatedUser)
}