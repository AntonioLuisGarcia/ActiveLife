package edu.tfc.activelife.dao

data class Routine(
    var title: String = "",
    var exercises: List<Exercise> = emptyList(),
    var userId: String = ""
) {
    constructor() : this("", emptyList(), "")
}
