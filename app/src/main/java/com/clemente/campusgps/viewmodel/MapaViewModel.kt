package com.clemente.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clemente.campusgps.data.LugaresRepository
import com.clemente.campusgps.data.UbicacionNoDisponibleException
import com.clemente.campusgps.data.UbicacionRepository
import com.clemente.campusgps.model.Lugar
import com.clemente.campusgps.model.LugarConDistancia
import com.clemente.campusgps.model.Posicion
import com.clemente.campusgps.model.TipoError
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
    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map {
            LugarConDistancia(it, null)
        }
    )

    val lugares: StateFlow<List<LugarConDistancia>> =
        _lugares.asStateFlow()

    private val _seleccionado =
        MutableStateFlow<Int?>(null)

    val seleccionado: StateFlow<Int?> =
        _seleccionado.asStateFlow()


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


    fun buscar() {

        // Comprobar primero Google Play Services
        if (!ubicacionRepo.playServicesDisponible()) {

            _estado.value = UbicacionUi.Error(
                mensaje = "Este dispositivo no tiene Google Play Services",
                tipo = TipoError.SIN_PLAY_SERVICES
            )

            return
        }

        _estado.value = UbicacionUi.Buscando

        viewModelScope.launch {

            ubicacionRepo.obtenerPosicion()
                .onSuccess { posicion ->
                    actualizarDistancias(posicion)

                    _estado.value =
                        UbicacionUi.Lista(posicion)
                }
                .onFailure { error ->
                    _estado.value = crearError(error)
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

    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }


    fun permisoDenegado() {

        _estado.value = UbicacionUi.Error(
            mensaje = "Sin permiso de ubicación no puedo mostrar dónde estás",
            tipo = TipoError.SIN_PERMISO
        )
    }


    // Identificar el tipo de error recibido
    private fun crearError(error: Throwable): UbicacionUi.Error {

        return when (error) {

            is SecurityException -> {

                UbicacionUi.Error(
                    mensaje = "Falta el permiso de ubicación",
                    tipo = TipoError.SIN_PERMISO
                )
            }

            is UbicacionNoDisponibleException -> {

                UbicacionUi.Error(
                    mensaje = "Activa el GPS e intenta de nuevo",
                    tipo = TipoError.GPS_APAGADO
                )
            }

            else -> {

                UbicacionUi.Error(
                    mensaje = error.message
                        ?: "No se pudo obtener la ubicación",
                    tipo = TipoError.OTRO
                )
            }
        }
    }
}

