package com.clemente.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.clemente.campusgps.model.Lugar
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
    masCercano: Lugar?,
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

        val lugar = lugares.firstOrNull {
            it.lugar.id == id
        }?.lugar ?: return@LaunchedEffect

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


        lugares.forEach { elemento ->

            val lugar = elemento.lugar

            key(lugar.id) {

                val esMasCercano =
                    lugar.id == masCercano?.id

                val colorMarcador = if (esMasCercano) {
                    BitmapDescriptorFactory.HUE_GREEN
                } else {
                    BitmapDescriptorFactory.HUE_AZURE
                }

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
                        colorMarcador
                    )
                )
            }
        }
    }
}
