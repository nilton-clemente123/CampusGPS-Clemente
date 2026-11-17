
package com.clemente.campusgps.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.core.content.ContextCompat
import com.clemente.campusgps.model.Posicion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class UbicacionRepositoryGoogle(
    private val context: Context
) : UbicacionRepository {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(
            context.applicationContext
        )

    private fun tienePermisoUbicacion(): Boolean {

        val permisoPreciso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return permisoPreciso || permisoAproximado
    }
    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> {

        return try {

            // Verificar si existe permiso de ubicación
            if (!tienePermisoUbicacion()) {
                throw SecurityException("Falta el permiso de ubicación")
            }

            // Solicitar la ubicación actual
            val ubicacion = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            // Verificar si se obtuvo una ubicación
            if (ubicacion == null) {
                throw Exception("No se pudo obtener la ubicación")
            }

            // Convertir las coordenadas al modelo Posicion
            Result.success(
                Posicion(
                    latitud = ubicacion.latitude,
                    longitud = ubicacion.longitude
                )
            )

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
