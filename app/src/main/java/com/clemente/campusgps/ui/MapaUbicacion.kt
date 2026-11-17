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

    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(-12.0464, -77.0428),
            12f
        )
    }

    LaunchedEffect(estado) {

        val ubicacion = estado as? UbicacionUi.Lista
            ?: return@LaunchedEffect

        val posicion = ubicacion.posicion

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

    LaunchedEffect(seleccionado) {

        val id = seleccionado ?: return@LaunchedEffect

        // Buscar el lugar seleccionado
        val lugar = lugares.firstOrNull {
            it.lugar.id == id
        }?.lugar ?: return@LaunchedEffect

        // Desplazar la cámara al lugar seleccionado
        camara.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    lugar.latitud,
                    lugar.longitud
                ),
                17f
            )
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = camara
    ) {

        // Marcador rojo: ubicación actual del usuario
        val ubicacion = estado as? UbicacionUi.Lista

        if (ubicacion != null) {

            val posicion = ubicacion.posicion

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

        // Marcadores azules: lugares del campus
        lugares.forEach { elemento ->

            val lugar = elemento.lugar

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
