package edu.tfc.activelife.dao

data class Exercise(
    var uuid: String = "",
    var name: String = "",
    var series: String = "",
    var repetitions: String = ""
) {
    constructor() : this("", "", "", "")
}
