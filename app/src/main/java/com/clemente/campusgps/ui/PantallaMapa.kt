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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clemente.campusgps.model.UbicacionUi
import com.clemente.campusgps.model.CategoriaFiltro
import com.clemente.campusgps.util.formatearDistancia
import com.clemente.campusgps.viewmodel.CampusViewModelFactory
import com.clemente.campusgps.viewmodel.MapaViewModel

internal val AzulCampus = Color(0xFF216DA6)
internal val FondoCampus = Color(0xFFFAF9FC)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa() {
    val context = LocalContext.current
    val vm: MapaViewModel = viewModel(factory = CampusViewModelFactory(context))
    val estado by vm.estado.collectAsState()
    val lugares by vm.lugares.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()
    val categoria by vm.categoria.collectAsState()

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            vm.buscar()
        } else {
            vm.permisoDenegado()
        }
    }

    val buscarUbicacion = {
        val preciso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val aproximado = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (preciso || aproximado) {
            vm.buscar()
        } else {
            permisoLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Scaffold(
        containerColor = FondoCampus,
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulCampus,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        val lugarMasCercano = masCercano
        val distanciaMasCercano = lugares.firstOrNull {
            it.lugar.id == lugarMasCercano?.id
        }?.metros

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (estado is UbicacionUi.Lista) {
                MapaUbicacion(
                    estado = estado,
                    lugares = lugares,
                    seleccionado = seleccionado,
                    masCercano = lugarMasCercano,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }

            Button(
                onClick = buscarUbicacion,
                enabled = estado !is UbicacionUi.Buscando,
                colors = ButtonDefaults.buttonColors(containerColor = AzulCampus),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Obtener mi ubicación", fontWeight = FontWeight.Bold)
            }

            when (val estadoActual = estado) {
                UbicacionUi.Inicial -> Text(
                    "Pulsa el botón para encontrar tu ubicación.",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
                UbicacionUi.Buscando -> Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = AzulCampus)
                    Text("Obteniendo ubicación...")
                }
                is UbicacionUi.Error -> MensajeError(
                    error = estadoActual,
                    onReintentar = buscarUbicacion,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                is UbicacionUi.Lista -> {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoriaFiltro.entries.forEach { opcion ->
                            FilterChip(
                                selected = categoria == opcion,
                                onClick = { vm.seleccionarCategoria(opcion) },
                                label = { Text(opcion.etiqueta) }
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { lugarMasCercano?.let { vm.seleccionar(it.id) } },
                        enabled = lugarMasCercano != null,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCampus)
                    ) {
                        Text("Ir al más cercano", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = if (lugarMasCercano != null && distanciaMasCercano != null) {
                            "Más cercano: ${lugarMasCercano.nombre} (${formatearDistancia(distanciaMasCercano)})"
                        } else {
                            "Más cercano: Sin calcular"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    ListaLugares(
                        lugares = lugares,
                        seleccionado = seleccionado,
                        masCercano = lugarMasCercano?.id,
                        onClick = vm::seleccionar,
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}
