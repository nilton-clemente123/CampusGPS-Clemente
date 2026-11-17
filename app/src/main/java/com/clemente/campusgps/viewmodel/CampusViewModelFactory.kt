
package com.clemente.campusgps.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clemente.campusgps.data.UbicacionRepositoryGoogle
import com.clemente.campusgps.data.LugaresRepositoryLocal

class CampusViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(MapaViewModel::class.java)) {

            val ubicacionRepository =
                UbicacionRepositoryGoogle(context)

            val lugaresRepository =
                LugaresRepositoryLocal()

            @Suppress("UNCHECKED_CAST")
            return MapaViewModel(
                ubicacionRepo = ubicacionRepository,
                lugaresRepo = lugaresRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
