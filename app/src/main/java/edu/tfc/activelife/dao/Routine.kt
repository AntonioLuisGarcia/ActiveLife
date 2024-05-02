package edu.tfc.activelife.dao

data class Routine(
    var id:String,
    var title: String = "",
    var exercises: List<BaseExercise> = emptyList(),
    var userUuid: String = ""
) {
    constructor() : this("") // Constructor vacío requerido por Firestore
}
