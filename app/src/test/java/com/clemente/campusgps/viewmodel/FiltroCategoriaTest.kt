package com.clemente.campusgps.viewmodel

import com.clemente.campusgps.model.CategoriaFiltro
import com.clemente.campusgps.model.Lugar
import com.clemente.campusgps.model.LugarConDistancia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltroCategoriaTest {
    private val lugares = listOf(
        lugar(1, "Comida", 10f),
        lugar(2, "Estudio", 20f),
        lugar(3, "Eventos", 30f),
        lugar(4, "estudio", 40f),
        lugar(5, "Servicios", 50f)
    )

    private fun lugar(id: Int, categoria: String, distancia: Float) = LugarConDistancia(
        Lugar(id, "Lugar $id", categoria, 0.0, 0.0), distancia
    )

    @Test
    fun todosConservaLaListaYElOrden() {
        assertEquals(lugares, filtrarLugares(lugares, CategoriaFiltro.TODOS))
    }

    @Test
    fun filtraCadaCategoriaSinAlterarElOrden() {
        assertEquals(listOf(2, 4), filtrarLugares(lugares, CategoriaFiltro.ESTUDIO).map { it.lugar.id })
        assertEquals(listOf(1), filtrarLugares(lugares, CategoriaFiltro.COMIDA).map { it.lugar.id })
        assertEquals(listOf(3), filtrarLugares(lugares, CategoriaFiltro.EVENTOS).map { it.lugar.id })
        assertEquals(listOf(5), filtrarLugares(lugares, CategoriaFiltro.SERVICIOS).map { it.lugar.id })
    }

    @Test
    fun categoriaSinResultadosDevuelveListaVacia() {
        assertTrue(filtrarLugares(emptyList(), CategoriaFiltro.SERVICIOS).isEmpty())
    }
}