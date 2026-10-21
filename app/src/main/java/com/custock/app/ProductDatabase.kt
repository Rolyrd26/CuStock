
package com.custock.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Producto(
    val id: Long = 0,
    val nombre: String,
    val categoria: String,
    val cantidad: Int,
    val coste: Double,
    val venta: Double,
    val referencia: String = "",
    val descripcion: String = ""
)

class ProductDatabase(context: Context) :
    SQLiteOpenHelper(context, "custock.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                categoria TEXT NOT NULL,
                cantidad INTEGER NOT NULL DEFAULT 0,
                coste REAL NOT NULL DEFAULT 0,
                venta REAL NOT NULL DEFAULT 0,
                referencia TEXT NOT NULL DEFAULT '',
                descripcion TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Aquí añadiremos futuras migraciones sin perder datos.
    }

    fun listar(
        categoria: String,
        busqueda: String = ""
    ): List<Producto> {
        val productos = mutableListOf<Producto>()

        val cursor = readableDatabase.query(
            "productos",
            null,
            "categoria = ? AND (nombre LIKE ? OR referencia LIKE ?)",
            arrayOf(
                categoria,
                "%$busqueda%",
                "%$busqueda%"
            ),
            null,
            null,
            "nombre COLLATE NOCASE ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                productos.add(
                    Producto(
                        id = it.getLong(
                            it.getColumnIndexOrThrow("id")
                        ),
                        nombre = it.getString(
                            it.getColumnIndexOrThrow("nombre")
                        ),
                        categoria = it.getString(
                            it.getColumnIndexOrThrow("categoria")
                        ),
                        cantidad = it.getInt(
                            it.getColumnIndexOrThrow("cantidad")
                        ),
                        coste = it.getDouble(
                            it.getColumnIndexOrThrow("coste")
                        ),
                        venta = it.getDouble(
                            it.getColumnIndexOrThrow("venta")
                        ),
                        referencia = it.getString(
                            it.getColumnIndexOrThrow("referencia")
                        ),
                        descripcion = it.getString(
                            it.getColumnIndexOrThrow("descripcion")
                        )
                    )
                )
            }
        }

        return productos
    }

    fun guardar(producto: Producto): Long {
        val valores = ContentValues().apply {
            put("nombre", producto.nombre)
            put("categoria", producto.categoria)
            put("cantidad", producto.cantidad)
            put("coste", producto.coste)
            put("venta", producto.venta)
            put("referencia", producto.referencia)
            put("descripcion", producto.descripcion)
        }

        return if (producto.id == 0L) {
            writableDatabase.insertOrThrow(
                "productos",
                null,
                valores
            )
        } else {
            val filas = writableDatabase.update(
                "productos",
                valores,
                "id = ?",
                arrayOf(producto.id.toString())
            )

            if (filas > 0) producto.id else -1L
        }
    }

    fun eliminar(id: Long) {
        writableDatabase.delete(
            "productos",
            "id = ?",
            arrayOf(id.toString())
        )
    }
}

