package com.clemente.campusgps.data

import com.clemente.campusgps.model.Lugar


interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
