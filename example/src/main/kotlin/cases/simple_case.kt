package main

fun main() {

    val user = generateUserRand()
    println("Simple case.: $user")

    // Optics
    val nameRef = UserZoom.name
    val ageRef = UserZoom.age
    val cityNameRef = UserZoom.address.city.name

    // Get data
    println("GET")
    println("Name........: ${nameRef.get(user)}")
    println("Age.........: ${ageRef.get(user)}")
    println("CityName....: ${cityNameRef.get(user)}")


    // Set data
    println("SET")
    val userWithUpdatedName = nameRef.set(user, "ZOOM")
    val userWithUpdatedNameAge = ageRef.set(userWithUpdatedName, 10)
    val userWithUpdatedNameAgeCityName = cityNameRef.set(userWithUpdatedNameAge, "KOSICE")
    println("After update: $userWithUpdatedNameAgeCityName")
}