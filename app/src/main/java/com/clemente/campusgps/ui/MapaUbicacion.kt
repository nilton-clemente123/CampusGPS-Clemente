package com.clemente.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.clemente.campusgps.model.LugarConDistancia
import com.clemente.campusgps.model.UbicacionUi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier
) {
    // Posición inicial: Lima
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(-12.0464, -77.0428),
            12f
        )
    }

    // Cuando obtenemos la ubicación, movemos la cámara
    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            val posicion = estado.posicion

            camara.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(posicion.latitud, posicion.longitud),
                    16f
                )
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = camara
    ) {
        // Mostrar marcador únicamente cuando tenemos ubicación
        if (estado is UbicacionUi.Lista) {
            val posicion = estado.posicion

            val marcador = rememberMarkerState(
                position = LatLng(
                    posicion.latitud,
                    posicion.longitud
                )
            )

            Marker(
                state = marcador,
                title = "Estás aquí"
            )
        }
    }
}
