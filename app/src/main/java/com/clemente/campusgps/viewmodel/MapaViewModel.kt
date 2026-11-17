package com.clemente.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clemente.campusgps.data.LugaresRepository
import com.clemente.campusgps.data.UbicacionRepository
import com.clemente.campusgps.model.Lugar
import com.clemente.campusgps.model.LugarConDistancia
import com.clemente.campusgps.model.Posicion
import com.clemente.campusgps.model.UbicacionUi
import com.clemente.campusgps.util.distanciaMetros
import com.clemente.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    // Estado de la ubicación
    private val _estado =
        MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)

    val estado: StateFlow<UbicacionUi> =
        _estado.asStateFlow()
    // Lista de lugares con sus distancias
    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map {
            LugarConDistancia(it, null)
        }
    )

    val lugares: StateFlow<List<LugarConDistancia>> =
        _lugares.asStateFlow()

    // Identificador del lugar seleccionado
    private val _seleccionado =
        MutableStateFlow<Int?>(null)

    val seleccionado: StateFlow<Int?> =
        _seleccionado.asStateFlow()


    // Obtener automáticamente el lugar más cercano
    val masCercano: StateFlow<Lugar?> =
        lugares.map { lista ->
            lista.firstOrNull {
                it.metros != null
            }?.lugar
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )


    // Buscar la ubicación actual
    fun buscar() {
        _estado.value = UbicacionUi.Buscando

        viewModelScope.launch {

            ubicacionRepo.obtenerPosicion()
                .onSuccess { posicion ->
                    actualizarDistancias(posicion)

                    _estado.value =
                        UbicacionUi.Lista(posicion)
                }
                .onFailure { error ->

                    _estado.value =
                        UbicacionUi.Error(mensajeDe(error))
                }
        }
    }


    private fun actualizarDistancias(posicion: Posicion) {

        val listaActualizada = lugaresRepo.obtenerLugares()

            .map { lugar ->
                LugarConDistancia(
                    lugar = lugar,
                    metros = distanciaMetros(
                        posicion,
                        lugar
                    )
                )
            }

            .sortedBy { it.metros }

        _lugares.value = listaActualizada

        // Registrar resultados en Logcat
        listaActualizada.forEach { elemento ->

            val distancia = elemento.metros

            if (distancia != null) {
                Log.d(
                    "CampusGPS",
                    "${elemento.lugar.nombre}: " +
                            formatearDistancia(distancia)
                )
            }
        }
    }

    fun seleccionar(id: Int?) {

        _seleccionado.value = id
    }


    fun permisoDenegado() {

        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás"
        )
    }


    private fun mensajeDe(e: Throwable): String = when (e) {

        is SecurityException ->
            "Falta el permiso de ubicación"

        else ->
            e.message ?: "No se pudo obtener la ubicación"
    }
}

