package com.clemente.campusgps.data

import com.clemente.campusgps.model.Lugar

class LugaresRepositoryLocal : LugaresRepository {

    override fun obtenerLugares(): List<Lugar> {
        return listOf(

            Lugar(
                id = 1,
                nombre = "Biblioteca Central",
                categoria = "Estudio",
                latitud = -12.044964217690318,
                longitud = -76.95286207627282
            ),

            Lugar(
                id = 2,
                nombre = "Cafetería Campus",
                categoria = "Comida",
                latitud = -12.044077456288676,
                longitud = -76.95217393889318
            ),

            Lugar(
                id = 3,
                nombre = "Laboratorio de Cómputo",
                categoria = "Estudio",
                latitud = -12.044469373519254,
                longitud = -76.95274873599853
            ),

            Lugar(
                id = 4,
                nombre = "Auditorio Principal",
                categoria = "Eventos",
                latitud = -12.044766280131315,
                longitud = -76.95278516680098
            ),

            Lugar(
                id = 5,
                nombre = "Estacionamiento Norte",
                categoria = "Servicios",
                latitud = -12.045106732642438,
                longitud = -76.95226703983279
            )
        )
    }
}
