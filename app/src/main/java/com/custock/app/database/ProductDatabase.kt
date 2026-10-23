package com.custock.app.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.custock.app.Fiado
import com.custock.app.LineaDocumento
import com.custock.app.LineaVenta
import com.custock.app.Movimiento
import com.custock.app.PagoFiadoDocumento
import com.custock.app.Producto
import com.custock.app.ResultadoVenta
import com.custock.app.VentaDocumento

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

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
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

    fun listar(categoria: String, busqueda: String = ""): List<Producto> {
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
        require(producto.nombre.isNotBlank()) { "El nombre del producto es obligatorio." }
        require(producto.cantidad >= 0) { "Las existencias no pueden ser negativas." }
        require(producto.coste >= 0 && producto.venta >= 0) { "Los precios no pueden ser negativos." }

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

        val db = writableDatabase
        return if (producto.id == 0L) {
            val nuevoId = db.insertOrThrow("productos", null, valores)

            if (producto.cantidad > 0) {
                db.insertOrThrow(
                    "movimientos",
                    null,
                    ContentValues().apply {
                        put("tipo", "ENTRADA")
                        put("producto_id", nuevoId)
                        put("nombre_producto", producto.nombre)
                        put("cantidad", producto.cantidad)
                        put("importe", producto.cantidad * producto.coste)
                        put("detalle", "Existencias iniciales")
                        put("fecha", System.currentTimeMillis())
                        put("moneda", producto.moneda)
                    }
                )
            }

            nuevoId
        } else {
            val anterior = obtenerProducto(producto.id)
            val filas = db.update("productos", valores, "id = ?", arrayOf(producto.id.toString()))

            if (filas > 0) {
                if (anterior != null && anterior.cantidad != producto.cantidad) {
                    val diferencia = producto.cantidad - anterior.cantidad
                    db.insertOrThrow(
                        "movimientos",
                        null,
                        ContentValues().apply {
                            put("tipo", if (diferencia > 0) "ENTRADA" else "SALIDA")
                            put("producto_id", producto.id)
                            put("nombre_producto", producto.nombre)
                            put("cantidad", kotlin.math.abs(diferencia))
                            put("importe", kotlin.math.abs(diferencia) * producto.coste)
                            put("detalle", "Ajuste manual de existencias")
                            put("fecha", System.currentTimeMillis())
                            put("moneda", producto.moneda)
                        }
                    )
                }

                producto.id
            } else {
                -1L
            }
        }
    }

    fun eliminar(id: Long) {
        writableDatabase.delete("productos", "id = ?", arrayOf(id.toString()))
    }

    fun listarMovimientos(): List<Movimiento> {
        val resultado = mutableListOf<Movimiento>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, tipo, producto_id, nombre_producto, cantidad, importe, detalle, fecha, moneda
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
                        productoId = if (it.isNull(2)) null else it.getLong(2),
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

    fun registrarVenta(
        lineas: List<LineaVenta>,
        cliente: String = "",
        esFiado: Boolean = false
    ): ResultadoVenta {
        require(lineas.isNotEmpty()) { "Añade al menos un producto." }
        require(!esFiado || cliente.isNotBlank()) { "Escribe el nombre del cliente para el fiado." }

        val db = writableDatabase
        db.beginTransaction()

        try {
            var total = 0.0
            val detalles = mutableListOf<Triple<Producto, LineaVenta, Double>>()

            for (linea in lineas) {
                val producto = obtenerProducto(linea.productoId)
                    ?: throw IllegalArgumentException("Un producto ya no existe.")

                require(producto.cantidad >= linea.cantidad) {
                    "No hay existencias suficientes de ${producto.nombre}."
                }

                val subtotal = linea.cantidad * linea.precioUnitario
                require(subtotal.isFinite()) { "El importe de la venta no es válido." }
                total += subtotal
                detalles.add(Triple(producto, linea, subtotal))
            }

            val monedasVenta = detalles.map { it.first.moneda }.distinct()
            require(monedasVenta.size == 1) {
                "No puedes mezclar monedas diferentes en una misma venta."
            }

            val monedaVenta = monedasVenta.first()
            val fecha = System.currentTimeMillis()

            val ventaId = db.insertOrThrow(
                "ventas",
                null,
                ContentValues().apply {
                    put("cliente", cliente.trim())
                    put("fecha", fecha)
                    put("total", total)
                    put("es_fiado", if (esFiado) 1 else 0)
                    put("moneda", monedaVenta)
                }
            )

            for ((producto, linea, subtotal) in detalles) {
                val actualizadas = db.update(
                    "productos",
                    ContentValues().apply { put("cantidad", producto.cantidad - linea.cantidad) },
                    "id = ? AND cantidad >= ?",
                    arrayOf(linea.productoId.toString(), linea.cantidad.toString())
                )

                if (actualizadas != 1) {
                    throw IllegalStateException("No se pudieron actualizar las existencias.")
                }

                db.insertOrThrow(
                    "detalle_ventas",
                    null,
                    ContentValues().apply {
                        put("venta_id", ventaId)
                        put("producto_id", producto.id)
                        put("nombre_producto", producto.nombre)
                        put("cantidad", linea.cantidad)
                        put("precio_unitario", linea.precioUnitario)
                        put("coste_unitario", producto.coste)
                        put("subtotal", subtotal)
                        put("moneda", monedaVenta)
                    }
                )

                db.insertOrThrow(
                    "movimientos",
                    null,
                    ContentValues().apply {
                        put("tipo", if (esFiado) "VENTA_FIADA" else "VENTA")
                        put("producto_id", producto.id)
                        put("nombre_producto", producto.nombre)
                        put("cantidad", linea.cantidad)
                        put("importe", subtotal)
                        put("detalle", "Venta #$ventaId a ${cliente.trim().ifBlank { "Cliente general" }}")
                        put("fecha", fecha)
                        put("moneda", producto.moneda)
                    }
                )
            }

            var fiadoId: Long? = null
            if (esFiado) {
                fiadoId = db.insertOrThrow(
                    "fiados",
                    null,
                    ContentValues().apply {
                        put("venta_id", ventaId)
                        put("cliente", cliente.trim())
                        put("total", total)
                        put("pagado", 0.0)
                        put("fecha", fecha)
                        put("moneda", monedaVenta)
                    }
                )
            }

            db.setTransactionSuccessful()
            return ResultadoVenta(ventaId, total, fiadoId)
        } finally {
            db.endTransaction()
        }
    }

    fun listarFiados(busquedaCliente: String = "", soloPendientes: Boolean = false): List<com.custock.app.Fiado> {
        val resultado = mutableListOf<com.custock.app.Fiado>()
        val condicion = buildString {
            append("cliente LIKE ?")
            if (soloPendientes) {
                append(" AND (total - pagado) > 0.009")
            }
        }

        val cursor = readableDatabase.query(
            "fiados",
            arrayOf("id", "venta_id", "cliente", "total", "pagado", "(total - pagado) AS saldo", "fecha", "moneda"),
            condicion,
            arrayOf("%$busquedaCliente%"),
            null,
            null,
            "fecha DESC, id DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                resultado.add(
                    com.custock.app.Fiado(
                        id = it.getLong(0),
                        ventaId = it.getLong(1),
                        cliente = it.getString(2),
                        total = it.getDouble(3),
                        pagado = it.getDouble(4),
                        saldo = it.getDouble(5),
                        fecha = it.getLong(6),
                        moneda = it.getString(7)
                    )
                )
            }
        }

        return resultado
    }

    fun registrarPago(fiadoId: Long, importe: Double): Long {
        require(importe.isFinite() && importe > 0) { "El pago debe ser mayor que cero." }

        val db = writableDatabase
        db.beginTransaction()

        try {
            val fiado = db.query(
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

            fiado.use {
                if (!it.moveToFirst()) throw IllegalArgumentException("El fiado no existe.")
                total = it.getDouble(0)
                pagado = it.getDouble(1)
                monedaFiado = it.getString(2)
            }

            val saldo = total - pagado
            require(saldo > 0.009) { "Este fiado ya está pagado." }
            require(importe <= saldo + 0.001) { "El pago supera el saldo pendiente." }

            val pagoId = db.insertOrThrow(
                "pagos_fiados",
                null,
                ContentValues().apply {
                    put("fiado_id", fiadoId)
                    put("importe", importe)
                    put("fecha", System.currentTimeMillis())
                    put("moneda", monedaFiado)
                }
            )

            val nuevoPagado = (pagado + importe).coerceAtMost(total)
            db.update(
                "fiados",
                ContentValues().apply { put("pagado", nuevoPagado) },
                "id = ?",
                arrayOf(fiadoId.toString())
            )

            db.setTransactionSuccessful()
            return pagoId
        } finally {
            db.endTransaction()
        }
    }

    fun productosDelFiado(ventaId: Long): List<LineaDocumento> {
        val resultado = mutableListOf<LineaDocumento>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT nombre_producto, cantidad, precio_unitario, subtotal, moneda
            FROM detalle_ventas
            WHERE venta_id = ?
            ORDER BY id ASC
            """.trimIndent(),
            arrayOf(ventaId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                resultado.add(
                    LineaDocumento(
                        nombre = it.getString(0),
                        cantidad = it.getInt(1),
                        precioUnitario = it.getDouble(2),
                        subtotal = it.getDouble(3),
                        moneda = it.getString(4)
                    )
                )
            }
        }

        return resultado
    }

    fun pagosDeFiado(fiadoId: Long): List<PagoFiadoDocumento> {
        val resultado = mutableListOf<PagoFiadoDocumento>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT p.id, p.fiado_id, f.cliente, p.importe, p.fecha, p.moneda
            FROM pagos_fiados p
            INNER JOIN fiados f ON f.id = p.fiado_id
            WHERE p.fiado_id = ?
            ORDER BY p.fecha ASC, p.id ASC
            """.trimIndent(),
            arrayOf(fiadoId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                resultado.add(
                    PagoFiadoDocumento(
                        id = it.getLong(0),
                        fiadoId = it.getLong(1),
                        cliente = it.getString(2),
                        importe = it.getDouble(3),
                        fecha = it.getLong(4),
                        moneda = it.getString(5)
                    )
                )
            }
        }

        return resultado
    }

    fun ventasDelPeriodo(desde: Long, hasta: Long): List<VentaDocumento> {
        val ventas = mutableListOf<VentaDocumento>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT id, cliente, fecha, total, es_fiado
            FROM ventas
            WHERE fecha >= ? AND fecha < ?
            ORDER BY fecha ASC, id ASC
            """.trimIndent(),
            arrayOf(desde.toString(), hasta.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                val ventaId = it.getLong(0)
                val lineas = mutableListOf<LineaDocumento>()

                val detalle = readableDatabase.rawQuery(
                    """
                    SELECT nombre_producto, cantidad, precio_unitario, subtotal, moneda
                    FROM detalle_ventas
                    WHERE venta_id = ?
                    ORDER BY id ASC
                    """.trimIndent(),
                    arrayOf(ventaId.toString())
                )

                detalle.use { d ->
                    while (d.moveToNext()) {
                        lineas.add(
                            LineaDocumento(
                                nombre = d.getString(0),
                                cantidad = d.getInt(1),
                                precioUnitario = d.getDouble(2),
                                subtotal = d.getDouble(3),
                                moneda = d.getString(4)
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

    fun pagosFiadosDelPeriodo(desde: Long, hasta: Long): List<PagoFiadoDocumento> {
        val pagos = mutableListOf<PagoFiadoDocumento>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT p.id, p.fiado_id, f.cliente, p.importe, p.fecha, p.moneda
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
                        fecha = it.getLong(4),
                        moneda = it.getString(5)
                    )
                )
            }
        }

        return pagos
    }

    fun totalVentas(desde: Long, hasta: Long): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE fecha >= ? AND fecha < ?",
            arrayOf(desde.toString(), hasta.toString())
        )

        cursor.use {
            return if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }

    fun totalVentasPorMoneda(desde: Long, hasta: Long): Map<String, Double> {
        val totales = mutableMapOf<String, Double>()
        val cursor = readableDatabase.rawQuery(
            "SELECT moneda, COALESCE(SUM(total), 0) FROM ventas WHERE fecha >= ? AND fecha < ? GROUP BY moneda",
            arrayOf(desde.toString(), hasta.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                totales[it.getString(0)] = it.getDouble(1)
            }
        }

        return totales
    }
    
    fun gananciaPorMoneda(desde: Long, hasta: Long): Map<String, Double> {
        val ganancias = mutableMapOf<String, Double>()
        val cursor = readableDatabase.rawQuery(
            """
            SELECT dv.moneda,
                   COALESCE(SUM(dv.cantidad * (dv.precio_unitario - dv.coste_unitario)), 0)
            FROM detalle_ventas dv
            INNER JOIN ventas v ON v.id = dv.venta_id
            WHERE v.fecha >= ? AND v.fecha < ?
            GROUP BY dv.moneda
            """.trimIndent(),
            arrayOf(desde.toString(), hasta.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                ganancias[it.getString(0)] = it.getDouble(1)
            }
        }

        return ganancias
    }


    fun gananciaRegistrada(desde: Long, hasta: Long): Double {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(dv.cantidad * (dv.precio_unitario - dv.coste_unitario)), 0)
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
