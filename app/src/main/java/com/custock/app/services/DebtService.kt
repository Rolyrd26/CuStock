package com.custock.app.services

import com.custock.app.Fiado
import com.custock.app.database.ProductDatabase

class DebtService(
    private val db: ProductDatabase
) {
    fun listarFiados(busquedaCliente: String = "", soloPendientes: Boolean = false): List<Fiado> =
        db.listarFiados(busquedaCliente, soloPendientes)

    fun registrarPago(fiadoId: Long, importe: Double): Long {
        require(importe.isFinite() && importe > 0) { "El pago debe ser mayor que cero." }
        return db.registrarPago(fiadoId, importe)
    }
}