
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
    val fecha: Long
)

data class PagoFiado(
    val id: Long,
    val fiadoId: Long,
    val importe: Double,
    val fecha: Long
)
data class LineaDocumento(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
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
    val fecha: Long
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
class ProductDatabase(context: Context) :
    SQLiteOpenHelper(context, "custock.db", null, 5) {
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
descripcion TEXT NOT NULL DEFAULT '',
moneda TEXT NOT NULL DEFAULT 'CUP'
            )
            """.trimIndent()
        )

                crearTablasVentas(db)
        crearTablaMovimientos(db)
    }

    private fun crearTablasVentas(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ventas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente TEXT NOT NULL DEFAULT '',
                fecha INTEGER NOT NULL,
                total REAL NOT NULL,
es_fiado INTEGER NOT NULL DEFAULT 0,
moneda TEXT NOT NULL DEFAULT 'CUP'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS detalle_ventas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                venta_id INTEGER NOT NULL,
                producto_id INTEGER NOT NULL,
                nombre_producto TEXT NOT NULL,
                cantidad INTEGER NOT NULL,
                precio_unitario REAL NOT NULL,
                coste_unitario REAL NOT NULL,
subtotal REAL NOT NULL,
moneda TEXT NOT NULL DEFAULT 'CUP',
FOREIGN KEY(venta_id) REFERENCES ventas(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fiados (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                venta_id INTEGER NOT NULL UNIQUE,
                cliente TEXT NOT NULL,
                total REAL NOT NULL,
                pagado REAL NOT NULL DEFAULT 0,
fecha INTEGER NOT NULL,
moneda TEXT NOT NULL DEFAULT 'CUP',
FOREIGN KEY(venta_id) REFERENCES ventas(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pagos_fiados (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fiado_id INTEGER NOT NULL,
                importe REAL NOT NULL,
fecha INTEGER NOT NULL,
moneda TEXT NOT NULL DEFAULT 'CUP',
FOREIGN KEY(fiado_id) REFERENCES fiados(id)
            )
            """.trimIndent()
        )
    }
    private fun crearTablaMovimientos(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS movimientos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT NOT NULL,
                producto_id INTEGER,
                nombre_producto TEXT NOT NULL DEFAULT '',
                cantidad INTEGER NOT NULL DEFAULT 0,
                importe REAL NOT NULL DEFAULT 0,
detalle TEXT NOT NULL DEFAULT '',
fecha INTEGER NOT NULL,
moneda TEXT NOT NULL DEFAULT 'CUP'
            )
            """.trimIndent()
        )
    }
        
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            crearTablasVentas(db)
        }

        if (oldVersion < 3) {
            crearTablaMovimientos(db)
        }

        if (oldVersion < 4) {
            db.execSQL(
                "ALTER TABLE productos ADD COLUMN moneda TEXT NOT NULL DEFAULT 'CUP'"
            )
        }

        
        if (oldVersion < 5) {
            if (oldVersion >= 2) {
                db.execSQL(
                    "ALTER TABLE ventas ADD COLUMN moneda TEXT NOT NULL DEFAULT 'CUP'"
                )
                db.execSQL(
                    "ALTER TABLE detalle_ventas ADD COLUMN moneda TEXT NOT NULL DEFAULT 'CUP'"
                )
                db.execSQL(
                    "ALTER TABLE fiados ADD COLUMN moneda TEXT NOT NULL DEFAULT 'CUP'"
                )
                db.execSQL(
                    "ALTER TABLE pagos_fiados ADD COLUMN moneda TEXT NOT NULL DEFAULT 'CUP'"
                )
            }

            if (oldVersion >= 3) {
                db.execSQL(
                    "ALTER TABLE movimientos ADD COLUMN moneda TEXT NOT NULL DEFAULT 'CUP'"
                )
            }
        }
    }

    fun listarMovimientos(): List<Movimiento> {
        val resultado = mutableListOf<Movimiento>()

        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, tipo, producto_id, nombre_producto,
       cantidad, importe, detalle, fecha, moneda
            FROM movimientos
            ORDER BY fecha DESC, id DESC
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                resultado.add(
                    Movimiento(
                        id = it.getLong(0),
                        tipo = it.getString(1),
                        productoId = if (it.isNull(2)) {
                            null
                        } else {
                            it.getLong(2)
                        },
                        nombreProducto = it.getString(3),
                        cantidad = it.getInt(4),
                        importe = it.getDouble(5),
                        detalle = it.getString(6),
                        fecha = it.getLong(7),
                        moneda = it.getString(8)
                    )
                )
            }
        }

        return resultado
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
            arrayOf(categoria, "%$busqueda%", "%$busqueda%"),
            null,
            null,
            "nombre COLLATE NOCASE ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                productos.add(
                    Producto(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                        categoria = it.getString(it.getColumnIndexOrThrow("categoria")),
                        cantidad = it.getInt(it.getColumnIndexOrThrow("cantidad")),
                        coste = it.getDouble(it.getColumnIndexOrThrow("coste")),
                        venta = it.getDouble(it.getColumnIndexOrThrow("venta")),
                        referencia = it.getString(it.getColumnIndexOrThrow("referencia")),
                        descripcion = it.getString(it.getColumnIndexOrThrow("descripcion")),
                        moneda = it.getString(it.getColumnIndexOrThrow("moneda"))
                    )
                )
            }
        }

        return productos
    }

    fun obtenerProducto(id: Long): Producto? {
        val cursor = readableDatabase.query(
            "productos",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (!it.moveToFirst()) return null

            return Producto(
                id = it.getLong(it.getColumnIndexOrThrow("id")),
                nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                categoria = it.getString(it.getColumnIndexOrThrow("categoria")),
                cantidad = it.getInt(it.getColumnIndexOrThrow("cantidad")),
                coste = it.getDouble(it.getColumnIndexOrThrow("coste")),
                venta = it.getDouble(it.getColumnIndexOrThrow("venta")),
                referencia = it.getString(it.getColumnIndexOrThrow("referencia")),
                descripcion = it.getString(it.getColumnIndexOrThrow("descripcion")),
                moneda = it.getString(it.getColumnIndexOrThrow("moneda"))
            )
        }
    }

    fun guardar(producto: Producto): Long {
        require(producto.nombre.isNotBlank()) {
            "El nombre del producto es obligatorio."
        }
        require(producto.cantidad >= 0) {
            "Las existencias no pueden ser negativas."
        }
        require(
            producto.coste.isFinite() &&
                producto.venta.isFinite() &&
                producto.coste >= 0 &&
                producto.venta >= 0
        ) {
            "Los precios deben ser válidos y no negativos."
        }

        val valores = ContentValues().apply {
            put("nombre", producto.nombre)
            put("categoria", producto.categoria)
            put("cantidad", producto.cantidad)
            put("coste", producto.coste)
            put("venta", producto.venta)
            put("referencia", producto.referencia)
            put("descripcion", producto.descripcion)
            put("moneda", producto.moneda)
        }

                return if (producto.id == 0L) {
            val db = writableDatabase
            val nuevoId = db.insertOrThrow(
                "productos",
                null,
                valores
            )

            if (producto.cantidad > 0) {
                val movimiento = ContentValues().apply {
                    put("tipo", "ENTRADA")
                    put("producto_id", nuevoId)
                    put("nombre_producto", producto.nombre)
                    put("cantidad", producto.cantidad)
                    put("importe", producto.cantidad * producto.coste)
                    put("detalle", "Existencias iniciales")
                    put("fecha", System.currentTimeMillis())
                    put("moneda", producto.moneda)
                }

                db.insertOrThrow(
                    "movimientos",
                    null,
                    movimiento
                )
            }

            nuevoId
                } else {
            val db = writableDatabase
            val anterior = obtenerProducto(producto.id)

            val filas = db.update(
                "productos",
                valores,
                "id = ?",
                arrayOf(producto.id.toString())
            )

            if (filas > 0) {
                if (anterior != null &&
                    anterior.cantidad != producto.cantidad
                ) {
                    val diferencia =
                        producto.cantidad - anterior.cantidad

                    val movimiento = ContentValues().apply {
                        put(
                            "tipo",
                            if (diferencia > 0) "ENTRADA"
                            else "SALIDA"
                        )
                        put("producto_id", producto.id)
                        put("nombre_producto", producto.nombre)
                        put("cantidad", kotlin.math.abs(diferencia))
                        put(
                            "importe",
                            kotlin.math.abs(diferencia) * producto.coste
                        )
                        put("detalle", "Ajuste manual de existencias")
                        put("fecha", System.currentTimeMillis())
                        put("moneda", producto.moneda)
                    }

                    db.insertOrThrow(
                        "movimientos",
                        null,
                        movimiento
                    )
                }

                producto.id
            } else {
                -1L
            }
                }
    }

    fun eliminar(id: Long) {
        writableDatabase.delete(
            "productos",
            "id = ?",
            arrayOf(id.toString())
        )
    }

    /**
     * Registra una venta en una transacción.
     * Descuenta existencias y, si corresponde,
     * crea una deuda independiente.
     */
    fun registrarVenta(
        lineas: List<LineaVenta>,
        cliente: String = "",
        esFiado: Boolean = false
    ): ResultadoVenta {
        require(lineas.isNotEmpty()) {
            "Añade al menos un producto."
        }
        require(lineas.all {
            it.productoId > 0 &&
                it.cantidad > 0 &&
                it.precioUnitario.isFinite() &&
                it.precioUnitario >= 0
        }) {
            "Revisa las cantidades y los precios."
        }
        require(!esFiado || cliente.isNotBlank()) {
            "Escribe el nombre del cliente para el fiado."
        }

        val db = writableDatabase
        db.beginTransaction()

        try {
            var total = 0.0
            val detalles =
                mutableListOf<Triple<Producto, LineaVenta, Double>>()

            for (linea in lineas) {
                val producto = obtenerProducto(linea.productoId)
                    ?: throw IllegalArgumentException(
                        "Un producto ya no existe."
                    )

                require(producto.cantidad >= linea.cantidad) {
                    "No hay existencias suficientes de ${producto.nombre}."
                }

                val subtotal = linea.cantidad * linea.precioUnitario
                require(subtotal.isFinite()) {
                    "El importe de la venta no es válido."
                }

                total += subtotal
                detalles.add(Triple(producto, linea, subtotal))
            }

            require(total.isFinite()) {
                "El total de la venta no es válido."
            }
            val monedasVenta = detalles.map { it.first.moneda }.distinct()

require(monedasVenta.size == 1) {
    "No puedes mezclar monedas diferentes en una misma venta."
}

val monedaVenta = monedasVenta.first()

            val fecha = System.currentTimeMillis()
            val ventaValores = ContentValues().apply {
                put("cliente", cliente.trim())
                put("fecha", fecha)
                put("total", total)
                put("es_fiado", if (esFiado) 1 else 0)
            }

            val ventaId = db.insertOrThrow(
                "ventas", null, ventaValores
            )

            for ((producto, linea, subtotal) in detalles) {
                val actualizadas = db.update(
                    "productos",
                    ContentValues().apply {
                        put(
                            "cantidad",
                            producto.cantidad - linea.cantidad
                        )
                    },
                    "id = ? AND cantidad >= ?",
                    arrayOf(
                        linea.productoId.toString(),
                        linea.cantidad.toString()
                    )
                )

                if (actualizadas != 1) {
                    throw IllegalStateException(
                        "No se pudieron actualizar las existencias."
                    )
                }

                val detalleValores = ContentValues().apply {
                    put("venta_id", ventaId)
                    put("producto_id", producto.id)
                    put("nombre_producto", producto.nombre)
                    put("cantidad", linea.cantidad)
                    put("precio_unitario", linea.precioUnitario)
                    put("coste_unitario", producto.coste)
                    put("subtotal", subtotal)
                }

                db.insertOrThrow(
                    "detalle_ventas", null, detalleValores
                )                
                val movimientoValores = ContentValues().apply {
                    put("tipo", "VENTA")
                    put("producto_id", producto.id)
                    put("nombre_producto", producto.nombre)
                    put("cantidad", linea.cantidad)
                    put("importe", subtotal)
                    put(
                        "detalle",
                        "Venta #$ventaId a ${cliente.trim().ifBlank { "Cliente general" }}"
                    )
                    put("fecha", fecha)
                    put("moneda", producto.moneda)
                }

                db.insertOrThrow(
                    "movimientos",
                    null,
                    movimientoValores
                )
            }

            var fiadoId: Long? = null

            if (esFiado) {
                val fiadoValores = ContentValues().apply {
                    put("venta_id", ventaId)
                    put("cliente", cliente.trim())
                    put("total", total)
                    put("pagado", 0.0)
                    put("fecha", fecha)
                }

                fiadoId = db.insertOrThrow(
                    "fiados", null, fiadoValores
                )
            }

            db.setTransactionSuccessful()
            return ResultadoVenta(ventaId, total, fiadoId)
        } finally {
            db.endTransaction()
        }
    }

    fun listarFiados(
        busquedaCliente: String = "",
        soloPendientes: Boolean = false
    ): List<Fiado> {
        val resultado = mutableListOf<Fiado>()

        val condicion = buildString {
            append("cliente LIKE ?")
            if (soloPendientes) {
                append(" AND (total - pagado) > 0.009")
            }
        }

        val cursor = readableDatabase.query(
            "fiados",
            arrayOf(
                "id", "venta_id", "cliente", "total",
                "pagado", "(total - pagado) AS saldo", "fecha"
            ),
            condicion,
            arrayOf("%$busquedaCliente%"),
            null,
            null,
            "fecha DESC, id DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                resultado.add(
                    Fiado(
                        id = it.getLong(0),
                        ventaId = it.getLong(1),
                        cliente = it.getString(2),
                        total = it.getDouble(3),
                        pagado = it.getDouble(4),
                        saldo = it.getDouble(5),
                        fecha = it.getLong(6)
                    )
                )
            }
        }

        return resultado
    }

    fun registrarPago(fiadoId: Long, importe: Double): Long {
        require(importe.isFinite() && importe > 0) {
            "El pago debe ser mayor que cero."
        }

        val db = writableDatabase
        db.beginTransaction()

        try {
            val cursor = db.query(
                "fiados",
                arrayOf("total", "pagado", "moneda"),
                "id = ?",
                arrayOf(fiadoId.toString()),
                null,
                null,
                null
            )

            val total: Double
            val pagado: Double
            val monedaFiado: String

            cursor.use {
                if (!it.moveToFirst()) {
                    throw IllegalArgumentException("El fiado no existe.")
                }
                total = it.getDouble(0)
                pagado = it.getDouble(1)
                monedaFiado = it.getString(2)
            }

            val saldo = total - pagado

            require(saldo > 0.009) {
                "Este fiado ya está pagado."
            }
            require(importe <= saldo + 0.001) {
                "El pago supera el saldo pendiente."
            }

            val pagoValores = ContentValues().apply {
                put("fiado_id", fiadoId)
                put("importe", importe)
                put("fecha", System.currentTimeMillis())
                put("moneda", monedaFiado)
            }

            val pagoId = db.insertOrThrow(
                "pagos_fiados", null, pagoValores
            )
            val datosFiado = db.query(
                "fiados",
                arrayOf("cliente", "venta_id", "moneda"),
                "id = ?",
                arrayOf(fiadoId.toString()),
                null,
                null,
                null
            )

            val clientePago: String
            val ventaPago: Long
            val monedaPago: String

            datosFiado.use {
                if (!it.moveToFirst()) {
                    throw IllegalArgumentException("El fiado no existe.")
                }
                clientePago = it.getString(0)
                ventaPago = it.getLong(1)
                monedaPago = it.getString(2)
            }

            val movimientoPago = ContentValues().apply {
                put("tipo", "PAGO_FIADO")
                put("nombre_producto", clientePago)
                put("cantidad", 0)
                put("importe", importe)
                put(
                    "detalle",
                    "Pago del fiado #$ventaPago"
                )
                put("fecha", System.currentTimeMillis())
                put("moneda", monedaPago)
            }

            db.insertOrThrow(
                "movimientos",
                null,
                movimientoPago
            )
            val nuevoPagado = (pagado + importe).coerceAtMost(total)
            val actualizacion = ContentValues().apply {
                put("pagado", nuevoPagado)
            }

            val filas = db.update(
                "fiados",
                actualizacion,
                "id = ?",
                arrayOf(fiadoId.toString())
            )

            if (filas != 1) {
                throw IllegalStateException(
                    "No se pudo actualizar el saldo."
                )
            }

            db.setTransactionSuccessful()
            return pagoId
        } finally {
            db.endTransaction()
        }
    }
    fun productosDelFiado(ventaId: Long): List<LineaDocumento> {
        val lineas = mutableListOf<LineaDocumento>()

        val cursor = readableDatabase.rawQuery(
            "SELECT nombre_producto, cantidad, precio_unitario, subtotal " +
                "FROM detalle_ventas WHERE venta_id = ? ORDER BY id ASC",
            arrayOf(ventaId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                lineas.add(
                    LineaDocumento(
                        nombre = it.getString(0),
                        cantidad = it.getInt(1),
                        precioUnitario = it.getDouble(2),
                        subtotal = it.getDouble(3)
                    )
                )
            }
        }

        return lineas
    }
    fun historialPagos(fiadoId: Long): List<PagoFiado> {
        val pagos = mutableListOf<PagoFiado>()

        val cursor = readableDatabase.query(
            "pagos_fiados",
            arrayOf("id", "fiado_id", "importe", "fecha"),
            "fiado_id = ?",
            arrayOf(fiadoId.toString()),
            null,
            null,
            "fecha DESC, id DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                pagos.add(
                    PagoFiado(
                        id = it.getLong(0),
                        fiadoId = it.getLong(1),
                        importe = it.getDouble(2),
                        fecha = it.getLong(3)
                    )
                )
            }
        }

        return pagos
    }
fun ventasDelPeriodo(
    desde: Long,
    hasta: Long
): List<VentaDocumento> {
    val ventas = mutableListOf<VentaDocumento>()

    val cursor = readableDatabase.rawQuery(
        "SELECT id, cliente, fecha, total, es_fiado " +
            "FROM ventas WHERE fecha >= ? AND fecha < ? " +
            "ORDER BY fecha ASC, id ASC",
        arrayOf(desde.toString(), hasta.toString())
    )

    cursor.use {
        while (it.moveToNext()) {
            val ventaId = it.getLong(0)
            val lineas = mutableListOf<LineaDocumento>()

            val detalle = readableDatabase.rawQuery(
                "SELECT nombre_producto, cantidad, precio_unitario, subtotal " +
                    "FROM detalle_ventas WHERE venta_id = ? ORDER BY id ASC",
                arrayOf(ventaId.toString())
            )

            detalle.use { d ->
                while (d.moveToNext()) {
                    lineas.add(
                        LineaDocumento(
                            nombre = d.getString(0),
                            cantidad = d.getInt(1),
                            precioUnitario = d.getDouble(2),
                            subtotal = d.getDouble(3)
                        )
                    )
                }
            }

            ventas.add(
                VentaDocumento(
                    id = ventaId,
                    cliente = it.getString(1),
                    fecha = it.getLong(2),
                    total = it.getDouble(3),
                    esFiado = it.getInt(4) == 1,
                    lineas = lineas
                )
            )
        }
    }

    return ventas
}

fun pagosFiadosDelPeriodo(
    desde: Long,
    hasta: Long
): List<PagoFiadoDocumento> {
    val pagos = mutableListOf<PagoFiadoDocumento>()

    val cursor = readableDatabase.rawQuery(
        """
        SELECT p.id, p.fiado_id, f.cliente, p.importe, p.fecha
        FROM pagos_fiados p
        INNER JOIN fiados f ON f.id = p.fiado_id
        WHERE p.fecha >= ? AND p.fecha < ?
        ORDER BY p.fecha ASC, p.id ASC
        """.trimIndent(),
        arrayOf(desde.toString(), hasta.toString())
    )

    cursor.use {
        while (it.moveToNext()) {
            pagos.add(
                PagoFiadoDocumento(
                    id = it.getLong(0),
                    fiadoId = it.getLong(1),
                    cliente = it.getString(2),
                    importe = it.getDouble(3),
                    fecha = it.getLong(4)
                )
            )
        }
    }

    return pagos
}
    fun totalVentas(desde: Long, hasta: Long): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(total), 0) FROM ventas " +
                "WHERE fecha >= ? AND fecha < ?",
            arrayOf(desde.toString(), hasta.toString())
        )

        cursor.use {
            return if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }

    fun gananciaRegistrada(desde: Long, hasta: Long): Double {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(
                dv.cantidad *
                (dv.precio_unitario - dv.coste_unitario)
            ), 0)
            FROM detalle_ventas dv
            INNER JOIN ventas v ON v.id = dv.venta_id
            WHERE v.fecha >= ? AND v.fecha < ?
            """.trimIndent(),
            arrayOf(desde.toString(), hasta.toString())
        )

        cursor.use {
            return if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }
}
