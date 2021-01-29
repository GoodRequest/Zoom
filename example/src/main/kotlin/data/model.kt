package main

import zoom.Zoom

enum class Gender { MALE, FEMALE }

const val AMOUNT = 5

@Zoom("MyZoom") //Name your optics
data class State(
        val usersList: List<User> = generateXUsers(AMOUNT),
        val usersMap: Map<String, User> = generateXUsers(AMOUNT).associateBy { it.name }
)

@Zoom
data class User(
        val name: String,
        val age: Int?,
        val address: Address,
        val gender: Gender?
)

@Zoom
data class Address(
        val city: City,
        val street: Street?
)

@Zoom
data class Street(
        val name: String,
        val number: Int
)

@Zoom
data class City(
        val name: String,
        val population: Int
)