package com.example.nimbus

data class World(val id: Long, val name: String, val about: String)
data class Person(val id: Long, val name: String, val description: String)
data class Place(val id: Long, val name: String, val description: String)
data class DictionaryEntry(val id: Long, val word: String, val meaning: String)

data class TimelineEntry(
    val id: Long,
    val title: String,
    val year: Int,
    val description: String,
    val placeName: String?,
    val people: String?
)