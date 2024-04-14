package edu.tfc.activelife.dao

data class Exercise(
    var uuid: String = "",
    var exerciseName: String = "",
    var series: String = "",
    var repetitions: String = ""
) {
    constructor() : this("", "", "", "")
}
