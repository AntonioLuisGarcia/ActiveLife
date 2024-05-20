package edu.tfc.activelife.ui.fragments

import android.util.Log
import edu.tfc.activelife.dao.CitaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.tfc.activelife.dao.CitaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitasViewModel @Inject constructor(
    private val repository: CitaRepository): ViewModel() {

    private val _uiState = MutableStateFlow(CitasUIState(citas = listOf(), errorMessage = ""))
    val uiState: StateFlow<CitasUIState> get() = _uiState.asStateFlow()

    init {
        fetchCitas()
    }

    private fun fetchCitas() {
        viewModelScope.launch(Dispatchers.IO) {
            val citas = repository.getAllCitas().value ?: emptyList()
            _uiState.value = CitasUIState(citas = citas)
        }
    }

    fun addCita(cita: CitaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addCita(cita)
                fetchCitas() // Refresh the list
            } catch (e: Exception) {
                Log.e("CitasViewModel", "Error adding cita", e)
            }
        }
    }

    fun removeCita(citaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Aquí puedes implementar la lógica para eliminar una cita si es necesario
            fetchCitas() // Refresh the list
        }
    }
}

data class CitasUIState(
    val citas: List<CitaEntity>,
    val errorMessage: String = ""
)
