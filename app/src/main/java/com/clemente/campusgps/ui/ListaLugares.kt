package com.clemente.campusgps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clemente.campusgps.model.LugarConDistancia
import com.clemente.campusgps.util.formatearDistancia

@Composable
fun ListaLugares(
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercano: Int?,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items = lugares, key = { it.lugar.id }) { elemento ->
            val lugar = elemento.lugar
            Card(
                onClick = { onClick(lugar.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (lugar.id == seleccionado) Color(0xFFDCEAF6) else Color(0xFFEDF0F5)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(10.dp).background(
                            if (lugar.id == masCercano) Color(0xFF4B9C50) else Color(0xFF4C86CE),
                            CircleShape
                        )
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lugar.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(lugar.categoria, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6C7280))
                    }
                    Text(
                        text = elemento.metros?.let { formatearDistancia(it) } ?: "Sin calcular",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AzulCampus
                    )
                }
            }
        }
    }
}
