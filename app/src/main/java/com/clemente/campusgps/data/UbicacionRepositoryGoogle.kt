package com.clemente.campusgps.data
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.clemente.campusgps.model.Posicion
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class UbicacionRepositoryGoogle(
    private val context: Context
) : UbicacionRepository {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(
            context.applicationContext
        )

    private fun tienePermisoPreciso(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun tienePermisoAproximado(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun tienePermisoUbicacion(): Boolean {
        return tienePermisoPreciso() || tienePermisoAproximado()
    }

    override fun playServicesDisponible(): Boolean {

        val resultado =
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context)

        return resultado == ConnectionResult.SUCCESS
    }

    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> {

        return try {

            if (!tienePermisoUbicacion()) {
                throw SecurityException(
                    "Falta el permiso de ubicación"
                )
            }

            val solicitud = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setGranularity(
                    Granularity.GRANULARITY_PERMISSION_LEVEL
                )
                .setMaxUpdateAgeMillis(0L)
                .setDurationMillis(20_000L)
                .build()

            val cancellationTokenSource =
                CancellationTokenSource()

            val ubicacion = fusedLocationClient.getCurrentLocation(
                solicitud,
                cancellationTokenSource.token
            ).await()

            // Si no se obtiene ubicación, informar el problema
            if (ubicacion == null) {
                throw UbicacionNoDisponibleException()
            }

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
