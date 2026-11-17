package com.clemente.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.clemente.campusgps.model.LugarConDistancia
import com.clemente.campusgps.model.UbicacionUi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier
) {

    // Posición inicial del mapa: Lima
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(-12.0464, -77.0428),
            12f
        )
    }

    // Mover la cámara cuando se obtiene la ubicación
    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {

            val posicion = estado.posicion

            camara.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        posicion.latitud,
                        posicion.longitud
                    ),
                    16f
                )
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = camara
    ) {

        // 1. Marcador rojo de la ubicación del usuario
        if (estado is UbicacionUi.Lista) {

            val posicion = estado.posicion

            Marker(
                state = rememberUpdatedMarkerState(
                    position = LatLng(
                        posicion.latitud,
                        posicion.longitud
                    )
                ),
                title = "Estás aquí"
            )
        }

        // 2. Marcadores azules de los cinco lugares
        lugares.forEach { lugarConDistancia ->

            val lugar = lugarConDistancia.lugar

            key(lugar.id) {

                Marker(
                    state = rememberUpdatedMarkerState(
                        position = LatLng(
                            lugar.latitud,
                            lugar.longitud
                        )
                    ),
                    title = lugar.nombre,
                    snippet = lugar.categoria,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
        }
    }
}
