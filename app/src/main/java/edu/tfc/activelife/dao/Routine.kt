package edu.tfc.activelife.dao

data class Routine(
    var id:String,
    var title: String = "",
    var exercises: List<BaseExercise> = emptyList(),
    var userUuid: String = "",
    var active: Boolean = false,
    var day : String
) {
    constructor() : this("", "", emptyList(), "",false,"") // Constructor vacío requerido por Firestore
}
