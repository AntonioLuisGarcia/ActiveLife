package edu.tfc.activelife.dao

data class Cita(
    var id: String,
    var userUuid: String, // Identificador único de la cita
    var descripcion: String, // Descripción de la cita
    var fecha: String // Fecha de la cita en formato de cadena (puedes cambiar al tipo Date si prefieres)
){
    constructor(): this("","","", "")
}