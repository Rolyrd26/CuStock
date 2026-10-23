package com.custock.app

data class Producto(
    val id: Long = 0,
    val nombre: String,
    val categoria: String,
    val cantidad: Int,
    val coste: Double,
    val venta: Double,
    val referencia: String = "",
    val descripcion: String = "",
    val moneda: String = "CUP"
)

data class LineaVenta(
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Double
)

data class ResultadoVenta(
    val ventaId: Long,
    val total: Double,
    val fiadoId: Long? = null
)

data class Fiado(
    val id: Long,
    val ventaId: Long,
    val cliente: String,
    val total: Double,
    val pagado: Double,
    val saldo: Double,
    val fecha: Long,
    val moneda: String = "CUP"
)

data class PagoFiado(
    val id: Long,
    val fiadoId: Long,
    val importe: Double,
    val fecha: Long,
    val moneda: String = "CUP"
)

data class LineaDocumento(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double,
    val moneda: String = "CUP"
)

data class VentaDocumento(
    val id: Long,
    val cliente: String,
    val fecha: Long,
    val total: Double,
    val esFiado: Boolean,
    val lineas: List<LineaDocumento>
)

data class PagoFiadoDocumento(
    val id: Long,
    val fiadoId: Long,
    val cliente: String,
    val importe: Double,
    val fecha: Long,
    val moneda: String = "CUP"
)

data class Movimiento(
    val id: Long,
    val tipo: String,
    val productoId: Long?,
    val nombreProducto: String,
    val cantidad: Int,
    val importe: Double,
    val detalle: String,
    val fecha: Long,
    val moneda: String = "CUP"
)
