package com.custock.app.services

import com.custock.app.LineaVenta
import com.custock.app.ResultadoVenta
import com.custock.app.database.ProductDatabase

class SalesService(
    private val db: ProductDatabase
) {
    fun registrarVenta(
        lineas: List<LineaVenta>,
        cliente: String = "",
        esFiado: Boolean = false
    ): ResultadoVenta {
        require(lineas.isNotEmpty()) { "Añade al menos un producto." }
        require(!esFiado || cliente.isNotBlank()) { "Escribe el nombre del cliente para registrar el fiado." }
        return db.registrarVenta(lineas, cliente, esFiado)
    }
}