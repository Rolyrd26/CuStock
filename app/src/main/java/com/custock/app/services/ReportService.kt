package com.custock.app.services

import com.custock.app.Movimiento
import com.custock.app.database.ProductDatabase

class ReportService(
    private val db: ProductDatabase
) {
    fun totalVentas(desde: Long, hasta: Long): Double =
        db.totalVentas(desde, hasta)

    fun totalVentasPorMoneda(desde: Long, hasta: Long): Map<String, Double> =
        db.totalVentasPorMoneda(desde, hasta)

    fun gananciaRegistrada(desde: Long, hasta: Long): Double =
        db.gananciaRegistrada(desde, hasta)

    fun listarMovimientos(): List<Movimiento> =
        db.listarMovimientos()

    fun ventasDelPeriodo(desde: Long, hasta: Long) =
        db.ventasDelPeriodo(desde, hasta)

    fun pagosFiadosDelPeriodo(desde: Long, hasta: Long) =
        db.pagosFiadosDelPeriodo(desde, hasta)
}