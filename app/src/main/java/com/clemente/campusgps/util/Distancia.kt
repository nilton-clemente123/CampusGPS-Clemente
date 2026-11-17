
package com.clemente.campusgps.util

import android.location.Location
import com.clemente.campusgps.model.Posicion
import com.clemente.campusgps.model.Lugar

fun distanciaMetros(
    desde: Posicion,
    hasta: Lugar
): Float {

    val resultado = FloatArray(1)

    Location.distanceBetween(
        desde.latitud,
        desde.longitud,
        hasta.latitud,
        hasta.longitud,
        resultado
    )

    return resultado[0]
}
