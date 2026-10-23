package com.custock.app.services

import com.custock.app.Producto
import com.custock.app.database.ProductDatabase

class InventoryService(
    private val db: ProductDatabase
) {
    fun listarCategoria(categoria: String, busqueda: String = ""): List<Producto> =
        db.listar(categoria, busqueda)

    fun obtenerProducto(id: Long): Producto? =
        db.obtenerProducto(id)

    fun guardarProducto(producto: Producto): Long {
        require(producto.nombre.isNotBlank()) { "El nombre del producto es obligatorio." }
        require(producto.cantidad >= 0) { "Las existencias no pueden ser negativas." }
        require(producto.coste >= 0 && producto.venta >= 0) { "Los precios no pueden ser negativos." }
        require(producto.moneda.isNotBlank()) { "La moneda es obligatoria." }

        return db.guardar(producto)
    }

    fun eliminarProducto(id: Long) {
        db.eliminar(id)
    }
}