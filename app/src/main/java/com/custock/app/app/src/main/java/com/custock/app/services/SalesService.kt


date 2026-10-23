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

        for (linea in lineas) {
            require(linea.productoId > 0) { "Identificador del producto inválido." }
            require(linea.cantidad > 0) { "La cantidad debe ser mayor que cero." }
            require(linea.precioUnitario.isFinite() && linea.precioUnitario >= 0) {
                "El precio unitario debe ser válido y no negativo."
            }
        }

        return db.registrarVenta(lineas, cliente, esFiado)
    }
}