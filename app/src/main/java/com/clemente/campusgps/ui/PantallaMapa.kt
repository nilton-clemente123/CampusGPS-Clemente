
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
import androidx.compose.foundation.layout.height
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
import com.clemente.campusgps.util.formatearDistancia
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
    val masCercano by vm.masCercano.collectAsState()


    val permisoLauncher =
        rememberLauncherForActivityResult(
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
                .padding(12.dp),

            verticalArrangement = Arrangement.spacedBy(8.dp)
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


            Button(

                onClick = {

                    masCercano?.let { lugar ->
                        vm.seleccionar(lugar.id)
                    }
                },

                enabled = masCercano != null,

                modifier = Modifier.fillMaxWidth()

            ) {
                Text("Ir al más cercano")
            }


            // Guardar el valor en una variable local
            // para evitar problemas de Smart Cast
            val lugarMasCercano = masCercano

            // Obtener la distancia del lugar más cercano
            val distanciaMasCercano =
                lugarMasCercano?.let { lugar ->

                    lugares.firstOrNull {
                        it.lugar.id == lugar.id
                    }?.metros
                }


            // Mostrar nombre y distancia del lugar más cercano
            Text(

                text = if (
                    lugarMasCercano != null &&
                    distanciaMasCercano != null
                ) {

                    "Más cercano: ${lugarMasCercano.nombre} " +
                            "(${formatearDistancia(distanciaMasCercano)})"

                } else {

                    "Más cercano: Sin calcular"
                },

                style = MaterialTheme.typography.bodyMedium
            )
            when (estado) {

                UbicacionUi.Inicial -> {
                    Text(
                        text = "Pulsa el botón para encontrar tu ubicación."
                    )
                }

                UbicacionUi.Buscando -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),

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

                    Text(
                        text = "Latitud: ${ubicacion.posicion.latitud}"
                    )

                    Text(
                        text = "Longitud: ${ubicacion.posicion.longitud}"
                    )
                }

                is UbicacionUi.Error -> {
                    val error = estado as UbicacionUi.Error

                    MensajeError(
                        error = error,
                        onReintentar = {
                            vm.buscar()
                        }
                    )
                }
            }
            MapaUbicacion(
                estado = estado,
                lugares = lugares,
                seleccionado = seleccionado,
                masCercano = lugarMasCercano,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            ListaLugares(

                lugares = lugares,
                seleccionado = seleccionado,

                onClick = { id ->
                    vm.seleccionar(id)
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}
