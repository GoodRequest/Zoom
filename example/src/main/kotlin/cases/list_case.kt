package main

import zoom.first
import zoom.index
import kotlin.random.Random

fun main() {
    var state = State()
    println("State.......: ${state.usersList}")

    val rIndex = Random.nextInt(0, AMOUNT)

    // Optics
    val uListRefI = MyZoom.usersList.index(rIndex)
    val nameRefI = uListRefI.name
    val ageRefI = uListRefI.age
    val cityNameRefI = uListRefI.address.city.name

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



    // Find first occurrence
    val uListRefF = MyZoom.usersList.first { it.age == null }
    val nameRefF = uListRefF.name
    val ageRefF = uListRefF.age
    val cityNameRefF = uListRefF.address.city.name

    // Get data
    println("\nGET")
    println("Name........: ${nameRefF.get(state)}")
    println("Age.........: ${ageRefF.get(state)}")
    println("CityName....: ${cityNameRefF.get(state)}")

    // Set data
    println("SET")
    state = nameRefI.set(state, "OPTICS")
    state = ageRefI.set(state, 15)
    state = cityNameRefI.set(state, "BUDAPEST")
    println("After update: $state")
}