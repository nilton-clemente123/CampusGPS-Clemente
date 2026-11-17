
package com.clemente.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clemente.campusgps.model.UbicacionUi
import com.clemente.campusgps.viewmodel.CampusViewModelFactory
import com.clemente.campusgps.viewmodel.MapaViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa() {

    val context = LocalContext.current

    val vm: MapaViewModel = viewModel(
        factory = CampusViewModelFactory(context)
    )

    val estado by vm.estado.collectAsState()
    val lugares by vm.lugares.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()

    // Solicitud de permisos de ubicación
    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->

        val concedido =
            permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (concedido) {
            vm.buscar()
        } else {
            vm.permisoDenegado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("CampusGPS")
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Button(
                onClick = {

                    val permisoPreciso =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    val permisoAproximado =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (permisoPreciso || permisoAproximado) {
                        vm.buscar()
                    } else {
                        permisoLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                enabled = estado !is UbicacionUi.Buscando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar mi ubicación")
            }

            // Mensajes según el estado
            when (estado) {

                UbicacionUi.Inicial -> {
                    Text(
                        text = "Pulsa el botón para encontrar tu ubicación.",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                UbicacionUi.Buscando -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()

                        Text(
                            text = "Obteniendo ubicación...",
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                is UbicacionUi.Lista -> {
                    val ubicacion = estado as UbicacionUi.Lista
                    val posicion = ubicacion.posicion

                    Text(
                        text = "Latitud: ${posicion.latitud}",
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "Longitud: ${posicion.longitud}",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                is UbicacionUi.Error -> {
                    val error = estado as UbicacionUi.Error

                    Text(
                        text = error.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Mapa de Google Maps
            MapaUbicacion(
                estado = estado,
                lugares = lugares,
                seleccionado = seleccionado,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
