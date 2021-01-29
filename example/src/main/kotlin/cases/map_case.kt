package main

import zoom.at

fun main() {
    var state = State()
    println("State.......: ${state.usersMap}")

    val nameAt = state.usersMap.keys.first()

    // Optics
    val uMapRef = MyZoom.usersMap.at(nameAt)
    val nameRefI = uMapRef.name
    val ageRefI = uMapRef.age
    val cityNameRefI = uMapRef.address.city.name

    // Get data
    println("GET")
    println("Name........: ${nameRefI.get(state)}")
    println("Age.........: ${ageRefI.get(state)}")
    println("CityName....: ${cityNameRefI.get(state)}")

    // Set data
    println("SET")
    state = nameRefI.set(state, "ZOOM")
    state = ageRefI.set(state, 10)
    state = cityNameRefI.set(state, "KOSICE")
    println("After update: $state")
}