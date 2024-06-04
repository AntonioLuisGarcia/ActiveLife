package edu.tfc.activelife.dao

import com.google.firebase.Timestamp

data class Cita(
    var id: String,
    var titulo: String, // Título de la cita
    var descripcion: String, // Descripción de la cita
    var fecha: String, // Fecha de la cita en formato de cadena (puedes cambiar al tipo Date si prefieres)
    val fechaCita: Timestamp, // Añadir este campo
    val image: String, // Nuevo campo para la URL de la imagen
    var encargado: String,
    var estado: String
){
    constructor(): this("","", "", "", Timestamp.now(), "","","")
}