package edu.tfc.activelife.dao

data class Routine(
    var id:String,
    var title: String = "",
    var exercises: List<Exercise> = emptyList(),
    var userUuid: String = ""
) {
    constructor() : this("") // Constructor vacío requerido por Firestore
}
