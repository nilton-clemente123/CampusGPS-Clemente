package com.clemente.campusgps.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clemente.campusgps.model.UbicacionUi
import com.clemente.campusgps.viewmodel.CampusViewModelFactory
import com.clemente.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa() {

    val contexto = LocalContext.current

    val vm: MapaViewModel = viewModel(
        factory = CampusViewModelFactory(contexto)
    )

    val estado by vm.estado.collectAsState()

    // Lanzador para solicitar los permisos
    val lanzador = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->

        if (resultado.values.any { it }) {
            vm.buscar()
        } else {
            vm.permisoDenegado()
        }
    }

    // Verificar si ya tenemos algún permiso
    fun alPulsar() {

        val permisoPreciso = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoPreciso || permisoAproximado) {
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = { alPulsar() },
                enabled = estado !is UbicacionUi.Buscando
            ) {
                Text("Obtener mi ubicación")
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (val resultado = estado) {

                is UbicacionUi.Inicial -> {
                    Text("Presiona el botón para obtener tu ubicación")
                }

                is UbicacionUi.Buscando -> {
                    CircularProgressIndicator()
                    Text("Obteniendo ubicación...")
                }

                is UbicacionUi.Lista -> {
                    Text("Ubicación obtenida correctamente")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Latitud: ${resultado.posicion.latitud}")
                    Text("Longitud: ${resultado.posicion.longitud}")
                }

                is UbicacionUi.Error -> {
                    Text(
                        text = resultado.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
