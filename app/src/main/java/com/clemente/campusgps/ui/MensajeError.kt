package com.clemente.campusgps.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clemente.campusgps.model.TipoError
import com.clemente.campusgps.model.UbicacionUi

@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error.mensaje,
            color = Color(0xFFAD3838),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFFF9E0E0), RoundedCornerShape(14.dp))
                .padding(16.dp)
        )
        when (error.tipo) {
            TipoError.SIN_PERMISO -> OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCampus)
            ) { Text("Abrir ajustes", fontWeight = FontWeight.Bold) }
            TipoError.GPS_APAGADO, TipoError.OTRO -> OutlinedButton(
                onClick = onReintentar,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCampus)
            ) { Text("Reintentar", fontWeight = FontWeight.Bold) }
            TipoError.SIN_PLAY_SERVICES -> Unit
        }
    }
}
