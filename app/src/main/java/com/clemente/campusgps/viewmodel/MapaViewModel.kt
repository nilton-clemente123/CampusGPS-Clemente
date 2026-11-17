package com.clemente.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clemente.campusgps.data.LugaresRepository
import com.clemente.campusgps.data.UbicacionRepository
import com.clemente.campusgps.model.LugarConDistancia
import com.clemente.campusgps.model.Posicion
import com.clemente.campusgps.model.UbicacionUi
import com.clemente.campusgps.util.distanciaMetros
import com.clemente.campusgps.util.formatearDistancia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    // Obtener la ubicación actual del usuario
    fun buscar() {
        _estado.value = UbicacionUi.Buscando

        viewModelScope.launch {

            ubicacionRepo.obtenerPosicion()
                .onSuccess { posicion ->
                    actualizarDistancias(posicion)

                    // Actualizar estado de ubicación
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

            // Ordenar desde el más cercano al más lejano
            .sortedBy { it.metros }

        // Publicar la lista actualizada
        _lugares.value = listaActualizada

        // Mostrar resultados en Logcat
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

    // Seleccionar un lugar de la lista
    fun seleccionar(id: Int?) {

        _seleccionado.value = id
    }


    // Manejar el rechazo del permiso
    fun permisoDenegado() {

        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás"
        )
    }


    // Obtener el mensaje de error
    private fun mensajeDe(e: Throwable): String = when (e) {

        is SecurityException ->
            "Falta el permiso de ubicación"

        else ->
            e.message ?: "No se pudo obtener la ubicación"
    }
}

